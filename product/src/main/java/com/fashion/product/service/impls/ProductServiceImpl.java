package com.fashion.product.service.impls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.util.AsyncUtils;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.request.ProductRequest.InnerInternalProductRequest;
import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.dto.response.ProductSkuResponse;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.dto.response.PromotionResponse.InnerPromotionResponse;
import com.fashion.product.dto.response.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.product.entity.Category;
import com.fashion.product.entity.OptionValue;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.entity.Variant;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.ProductMapper;
import com.fashion.product.mapper.ProductSkuMapper;
import com.fashion.product.properties.cache.ProductServiceCacheProperties;
import com.fashion.product.repository.CategoryRepository;
import com.fashion.product.repository.OptionValueRepository;
import com.fashion.product.repository.ProductRepository;
import com.fashion.product.repository.ProductSkuRepository;
import com.fashion.product.repository.ShopManagementRepository;
import com.fashion.product.repository.VariantRepository;
import com.fashion.product.service.ApprovalHistoryService;
import com.fashion.product.service.CategoryService;
import com.fashion.product.service.ProductService;
import com.fashion.product.service.ProductSkuService;
import com.fashion.product.service.PromotionService;
import com.fashion.product.service.RedisDistributedLocker;
import com.fashion.product.service.RedisDistributedService;
import com.fashion.product.service.RedisService;
import com.fashion.product.service.ShopManagementService;
import com.fashion.product.service.VariantService;
import com.fashion.product.service.provider.CacheProvider;
import com.google.common.cache.Cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService{
    ProductRepository productRepository;
    ShopManagementRepository shopManagementRepository;
    ShopManagementService shopManagementService;
    CategoryRepository categoryRepository;
    CategoryService categoryService;
    ProductMapper productMapper;
    ProductSkuRepository productSkuRepository;
    VariantRepository variantRepository;
    ProductSkuService productSkuService;
    OptionValueRepository optionValueRepository;
    ApprovalHistoryService approvalHistoryService;
    PromotionService promotionService;
    ProductSkuMapper productSkuMapper;
    ProductServiceCacheProperties productServiceCacheProperties;
    CacheProvider cacheProvider;
    VariantService variantService;
    Executor virtualExecutor;
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("PRODUCT-SERVICE: [createProduct] Start create product");
        try {
            Product product = this.productMapper.toValidated(request);
            ProductResponse productResponse = upSertProductOptimized(product,request.getVariants(),request.getCategory().getId(),request.getShopManagement().getId(), request.getVersion());
            this.updateProductCache(productResponse);
            return productResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    // @Override
    // @Transactional(rollbackFor = ServiceException.class)
    // public ProductResponse updateProduct(ProductRequest request) {
    //     log.info("PRODUCT-SERVICE: [updateProduct] Start update product");
    //     try {
    //         Product product = this.productRepository.lockProductById(request.getId()).orElseThrow(
    //             () -> new ServiceException(EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,"product.not.found.id", Map.of("id",request.getId()))
    //         );
    //         this.productMapper.toUpdate(product, request);
    //         product.getVariants().clear();
    //         this.variantRepository.deleteAllByProductId(product.getId());
    //         return upSertProduct(product,request.getVariants(),request.getCategory().getId(),request.getShopManagement().getId());
    //     } catch (ServiceException e) {
    //         throw e;
    //     } catch (Exception e) {
    //         log.error("PRODUCT-SERVICE: [updateProduct] Error: {}", e.getMessage(), e);
    //         throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
    //     }
    // }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public ProductResponse updateProduct(ProductRequest request) {
        log.info("PRODUCT-SERVICE: [updateProduct] Start update product");
        try {
            // Use pessimistic lock only for update
            Product product = this.productRepository.lockProductById(request.getId())
                .orElseThrow(() -> new ServiceException(
                    EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,
                    "product.not.found.id", 
                    Map.of("id", request.getId())
                ));
            
            this.productMapper.toUpdate(product, request);
            
            ProductResponse productResponse = upSertProductOptimized(
                product, 
                request.getVariants(), 
                request.getCategory().getId(), 
                request.getShopManagement().getId(),
                request.getVersion()
            );
            this.updateProductCache(productResponse);
            return productResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<ProductResponse>> getAllProduct(SearchRequest request) {
        try {
            SearchOption searchOption = request.getSearchOption();
            SearchModel searchModel = request.getSearchModel();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                searchOption.getPage(), 
                searchOption.getSize(), 
                searchOption.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(Product.class);

            Specification<Product> spec = new SpecificationUtils<Product>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Product> products = this.productRepository.findAll(spec, pageRequest);
            List<ProductResponse> productResponses = this.productMapper.toDto(products.getContent());
            return PageableUtils.<Product, ProductResponse>buildPaginationResponse(pageRequest, products, productResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public ProductResponse getProductById(UUID id, Long version) {
        try {
            String cacheKey = this.productServiceCacheProperties.createCacheKey(
                this.productServiceCacheProperties.getKeys().getProductInfo(),
                id
            );
            String lockKey = this.productServiceCacheProperties.createLockKey(
                this.productServiceCacheProperties.getKeys().getProductInfo(),
                id
            );
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, ProductResponse.class, () -> 
                productRepository.findById(id)
                    .map((p) -> {
                        ProductResponse productResponse = productMapper.toDto(p);
                        productResponse.setVersion(System.currentTimeMillis());
                        return productResponse;
                    })
                    .orElse(null)
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getProductById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findListProductById(List<UUID> ids) {
        try {
            return this.productRepository.findAllByIdIn(ids);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [findListProductById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateInternalProductById(UUID id, UUID skuId){
        try {
            Product product = this.productRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,"product.not.found.id", Map.of("id", id))
            );
            List<UUID> productSkuIds = product.getProductSkus().stream().map(ProductSku::getId).distinct().toList();
            if(!productSkuIds.contains(skuId)){
                throw new ServiceException(
                EnumError.PRODUCT_PRODUCT_SKU_ERR_NOT_FOUND_ID,
                "product.sku.not.found.id");
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [validateInternalProductById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getInternalProductByIdAndCheckApproval(InnerInternalProductRequest request) {
        try {
            List<UUID> productSkuIdList = request.getProductSkuIdList();
            List<UUID> productIdList = request.getProductIdList();
            List<Product> products = this.productRepository.findAllByIdIn(productIdList);
            if(products.isEmpty()){
                throw new ServiceException(EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID, "product.not.found.id.in");
            }
            
            // Check approval product
            this.approvalHistoryService.checkApprovalHistoryForUpSertOrder(products);

            // Check product sku
            List<ProductSku> targetSkus = products.stream()
                .flatMap(p -> p.getProductSkus().stream())
                .filter(sku -> productSkuIdList.contains(sku.getId()))
                .toList();

            if (targetSkus.size() != productSkuIdList.size()) {
                throw new ServiceException(EnumError.PRODUCT_PRODUCT_SKU_ERR_NOT_FOUND_ID, "product.sku.not.found.id.in");
            }

            // Grouping SKU following Product
            Map<UUID, List<ProductSku>> skuGroupedByProduct = targetSkus.stream()
                .collect(Collectors.groupingBy(sku -> sku.getProduct().getId()));

            // Get corresponding Promotion base on original product price
            return skuGroupedByProduct.entrySet().stream().map(entry -> {
                List<ProductSku> skus = entry.getValue();
                Product product = skus.get(0).getProduct();
                ProductResponse productRes = productMapper.toDto(product);

                List<InnerProductSkuResponse> skuResponses = skus.stream().map(sku -> {
                    InnerProductSkuResponse skuDto = productSkuMapper.toInnerEntity(sku);
                    
                    InnerPromotionResponse promotion = this.promotionService.getInternalCorrespondingPromotionByProductId(sku);
                    skuDto.setPromotion(promotion);
                    
                    return skuDto;
                }).toList();
                productRes.setProductSkus(skuResponses); 
                
                return productRes;
            }).toList();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getInternalProductByIdAndCheckApproval] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    private ProductResponse upSertProduct(
        Product product, 
        List<InnerVariantRequest> variants, 
        UUID categoryId, 
        UUID shopManagementId
    ){
        try {
            final String slug = SlugUtil.toSlug(product.getName());
            boolean isCreate = Objects.isNull(product.getId());
            checkExistedProduct(slug, product.getName(), categoryId, shopManagementId, product.getId());
            ShopManagement shopManagement = this.shopManagementRepository.findById(shopManagementId)
                .orElseThrow(
                    () -> new ServiceException(
                        EnumError.PRODUCT_SHOP_MANAGEMENT_ERR_NOT_FOUND_ID,
                        "shop.management.not.found.id",
                        Map.of("shopManagementId",shopManagementId)
                    )
                );
            Category category = this.categoryRepository.findById(categoryId)
                .map(c -> {
                    if(!this.categoryService.isLeaf(c))
                        throw new ServiceException(
                            EnumError.PRODUCT_CATEGORY_INVALID_ID, 
                            "product.category.invalid.id",
                            Map.of("categoryId", c.getId()
                        ));
                    return c;
                })
                .orElseThrow(
                    () -> new ServiceException(
                        EnumError.PRODUCT_CATEGORY_ERR_NOT_FOUND_ID,
                        "category.not.found.id",
                        Map.of("categoryId",product.getCategory().getId())
                    )
                );
            product.setSlug(slug);
            product.setCategory(category);
            product.setActivated(true);
            product.setShopManagement(shopManagement);
            Product createdProduct = productRepository.save(product);
            if(variants == null || variants.size() <= 0){
                return this.productMapper.toDto(createdProduct);
            } 
            final List<ProductSku> existingSku = this.productSkuRepository.findAllByProductId(createdProduct.getId());
            final Set<String> requestSkuIds = variants.stream().map(v -> v.getSkuId().toUpperCase()).distinct().collect(Collectors.toSet());
            final List<ProductSku> skusToDelete = existingSku.stream().filter(s -> !requestSkuIds.contains(s.getSku())).toList();
            if(skusToDelete.size() > 0){
                this.productSkuRepository.deleteAll(skusToDelete);
            }
            final Map<String, ProductSku> existingSkusMap = existingSku.stream()
                .collect(Collectors.toMap(s -> s.getSku().toUpperCase(), Function.identity(),
                    (a, b) -> a
                ));
            final List<ProductSku> newProductSku = new ArrayList<>();      
            for (InnerVariantRequest v : variants) {
                ProductSku sku = existingSkusMap.getOrDefault(v.getSkuId().toUpperCase(), new ProductSku());
                if (sku.getId() == null) {
                    // CREATE NEW SKU
                    ProductSku newSku = ProductSku.builder()
                            .sku(v.getSkuId().toUpperCase())
                            .price(v.getPrice())
                            .product(createdProduct)
                            .tempStock(v.getStock())
                            .build();
                    newProductSku.add(newSku);
                } else {
                    // UPDATE EXISTING SKU
                    int tempStock = v.getStock() > 0 ? v.getStock() + sku.getTempStock() : v.getStock();
                    sku.setSku(v.getSkuId().toUpperCase());
                    sku.setPrice(v.getPrice());
                    sku.setTempStock(tempStock);
                    newProductSku.add(sku);
                }
            }       
            final List<ProductSku> createdSku = productSkuRepository.saveAll(newProductSku);
            Map<String, ProductSku> allSkuMap = Stream.concat(createdSku.stream(), existingSku.stream())
                .collect(Collectors.toMap(s -> s.getSku().toUpperCase(), Function.identity(),
                    (a, b) -> a
                ));
            final List<String> optionValuesSlug = variants.stream()
                .flatMap(o -> o.getOptionValues().stream())
                .distinct()
                .collect(Collectors.toList());
            final Map<String, OptionValue> optionValueMap = 
                this.optionValueRepository.findAllBySlugIn(optionValuesSlug).stream()
                .collect(Collectors.toMap(OptionValue::getSlug, Function.identity()));
            final List<Variant> variantEntities = variants.stream()
            .filter(v -> allSkuMap.containsKey(v.getSkuId().toUpperCase()))
            .flatMap(v -> v.getOptionValues().stream()
                .map(optionValueMap::get)
                .filter(Objects::nonNull)
                .map(ov -> Variant.builder()
                    .product(createdProduct)
                    .productSku(allSkuMap.get(v.getSkuId().toUpperCase()))
                    .option(ov.getOption())
                    .optionValue(ov)
                    .activated(true)
                    .build())
            ).toList();
            if (!variantEntities.isEmpty()) {
                this.variantRepository.saveAll(variantEntities);
            }   
            this.approvalHistoryService.handleApprovalHistoryUpSertProduct(createdProduct,isCreate,ApprovalHistoryServiceImpl.ENTITY_TYPE_PRODUCT, null);
            return productMapper.toDto(createdProduct);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [upSertProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkExistedProduct(String slug, String name, UUID cateId, UUID smId, UUID excludedId){
        try {
            boolean exists;
            if (excludedId == null) {
                exists = productRepository.existsDuplicateForCreate(slug, cateId, smId);
            } else {
                exists = productRepository.existsDuplicateForUpdate(slug, cateId, smId, excludedId);
            }
            
            if (exists) {
                throw new ServiceException(
                    EnumError.PRODUCT_PRODUCT_DATA_EXISTED_NAME,
                    "product.exist.name",
                    Map.of("name", name)
                );
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkExistedProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }


    private ProductResponse upSertProductOptimized(
        Product product, 
        List<InnerVariantRequest> variants, 
        UUID categoryId, 
        UUID shopManagementId,
        Long version
    ) {
        try {
            final String slug = SlugUtil.toSlug(product.getName());
            boolean isCreate = Objects.isNull(product.getId());

            CompletableFuture<ShopManagement> shopManagementFuture = AsyncUtils.fetchAsyncWThread(
                () -> this.shopManagementService.fetchShopManagement(shopManagementId), virtualExecutor
            );
            
            CompletableFuture<Category> categoryFuture = AsyncUtils.fetchAsyncWThread(
                () -> this.categoryService.fetchAndValidateCategory(categoryId), virtualExecutor
            );

            CompletableFuture<Void> existenceCheckFuture = AsyncUtils.fetchVoidWThread(
                () -> checkExistedProduct(slug, product.getName(), categoryId, shopManagementId, product.getId()),
                virtualExecutor
            );
            try {
                CompletableFuture.allOf(shopManagementFuture, categoryFuture, existenceCheckFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }

            ShopManagement shopManagement = shopManagementFuture.join();
            Category category = categoryFuture.join();

            product.setSlug(slug);
            product.setCategory(category);
            product.setActivated(true);
            product.setShopManagement(shopManagement);

            Product savedProduct = productRepository.save(product);

            if (variants == null || variants.isEmpty()) {
                /**
                 * @param: Warning
                 * Không dùng non-blocking đây vì dùng virtual thread vì
                 *  Thread cha (Request Thread) nó trả về response cho khách hàng và ngay lập tức Tomcat thu hồi (recycle) 
                 *  đối tượng Request đó để dùng cho người khác nhằm tối ưu bộ nhớ
                 *  Thread con (Virtual Thread): Đang chạy ngầm, lúc này mới bắt đầu gọi identityClient
                 *  => Nên dùng blocking để chuẩn 1 luồng
                 */
                // AsyncUtils.fetchVoidWThread(
                //     () -> approvalHistoryService.handleApprovalHistoryUpSertProduct(
                //         savedProduct, isCreate, ApprovalHistoryServiceImpl.ENTITY_TYPE_PRODUCT
                //     ),
                //     virtualExecutor
                // );
                approvalHistoryService.handleApprovalHistoryUpSertProduct(
                    savedProduct, isCreate, ApprovalHistoryServiceImpl.ENTITY_TYPE_PRODUCT, version
                );
                return this.productMapper.toDto(savedProduct);
            }

            List<InnerProductSkuResponse> productSkuResponses = processVariantsOptimized(savedProduct, variants, isCreate, virtualExecutor);

            approvalHistoryService.handleApprovalHistoryUpSertProduct(
                savedProduct, isCreate, ApprovalHistoryServiceImpl.ENTITY_TYPE_PRODUCT, version
            );
            ProductResponse productResponse = productMapper.toDto(savedProduct);
            productResponse.setProductSkus(productSkuResponses);
            return productResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [upSertProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private List<InnerProductSkuResponse> processVariantsOptimized(Product saveProduct, List<InnerVariantRequest> variants, boolean isCreate, Executor virtualExecutor){
        try {
            final Set<String> requestSkuIds = variants.stream()
                .map(v -> v.getSkuId().toUpperCase()).collect(Collectors.toSet());

            final List<String> optionValuesSlug = variants.stream()
                .flatMap(v -> v.getOptionValues().stream())
                .distinct()
                .collect(Collectors.toList());

            CompletableFuture<List<ProductSku>> existingSkuFuture = !isCreate ? AsyncUtils.fetchAsyncWThread(
                () -> this.productSkuRepository.findAllByProductId(saveProduct.getId()),
                virtualExecutor
            ) : CompletableFuture.completedFuture(null);

            CompletableFuture<Map<String, OptionValue>> optionValueFuture = AsyncUtils.fetchAsyncWThread(
                () -> optionValueRepository.findAllBySlugIn(optionValuesSlug).stream()
                    .collect(Collectors.toMap(OptionValue::getSlug, Function.identity())),
                virtualExecutor
            );

            CompletableFuture<Void> deleteVariantsFuture = AsyncUtils.fetchVoidWThread(() -> {
                if (!isCreate) {
                    this.variantService.deleteAllByProductId(saveProduct.getId());
                }
            }, virtualExecutor);
            
            try {
                CompletableFuture.allOf(existingSkuFuture, optionValueFuture, deleteVariantsFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }

            List<ProductSku> existingSku = existingSkuFuture.join();
            Map<String, OptionValue> optionValueMap = optionValueFuture.join();

            if(!isCreate && !existingSku.isEmpty()){
                List<ProductSku> skusToDelete = existingSku.stream().filter(s -> !requestSkuIds.contains(s.getSku())).toList();
                /**
                 * @param batch: Gom vào 1 chuỗi lệnh SQL gửi đi 1 lần theo cấu hình ở application rồi dùng  
                 */
                if (!skusToDelete.isEmpty()) {
                    productSkuRepository.deleteAllInBatch(skusToDelete); // Use batch delete
                }
            }

            final Map<String, ProductSku> existingSkusMap = existingSku != null ?
                existingSku.stream()
                    .collect(Collectors.toMap(
                        s -> s.getSku().toUpperCase(), 
                        Function.identity(),
                        (a, b) -> a
                    )) :
                new HashMap<>()
                ;

            final List<ProductSku> skusToSave = new ArrayList<>(variants.size());
            for (InnerVariantRequest v : variants) {
                String skuId = v.getSkuId().toUpperCase();
                ProductSku sku = existingSkusMap.get(skuId);

                if (sku == null) {
                    // CREATE NEW SKU
                    ProductSku newSku = ProductSku.builder()
                        .sku(skuId)
                        .price(v.getPrice())
                        .product(saveProduct)
                        .tempStock(v.getStock())
                        .activated(true)
                        .eventId(UUID.randomUUID())
                        .build();
                    skusToSave.add(newSku);
                } else {
                    // UPDATE EXISTING SKU
                    int tempStock = v.getStock() > 0 ? v.getStock() + sku.getTempStock() : v.getStock();
                    sku.setSku(skuId);
                    sku.setPrice(v.getPrice());
                    sku.setTempStock(tempStock);
                    sku.setActivated(true);
                    sku.setEventId(UUID.randomUUID());
                    skusToSave.add(sku);
                }
            }

            final List<ProductSku> savedSkus = this.productSkuRepository.saveAll(skusToSave);
            
            Map<String, ProductSku> allSkuMap = savedSkus.stream()
                .collect(Collectors.toMap(
                    s -> s.getSku().toUpperCase(), 
                    Function.identity(),
                    (a, b) -> a
                ));
            
            CompletableFuture<Map<UUID, InnerPromotionResponse>> promotionFuture = AsyncUtils.fetchAsyncWThread(
                () -> promotionService.getBestPromotionsByProductIds(allSkuMap.values(), saveProduct.getId()),
                virtualExecutor
            );
            
            /**
             * @param Xóa trước khi thao tác với entity variant
             */
            deleteVariantsFuture.join();

            final List<Variant> variantEntities = variants.stream()
                .filter(v -> allSkuMap.containsKey(v.getSkuId().toUpperCase()))
                .flatMap(v -> {
                    ProductSku productSku = allSkuMap.get(v.getSkuId().toUpperCase());
                    return v.getOptionValues().stream()
                        .map(optionValueMap::get)
                        .filter(Objects::nonNull)
                        .map(ov -> Variant.builder()
                            .product(saveProduct)
                            .productSku(productSku)
                            .option(ov.getOption())
                            .optionValue(ov)
                            .activated(true)
                            .build());
                })
                .collect(Collectors.toList());

            if (!variantEntities.isEmpty()) {
                variantRepository.saveAll(variantEntities);
            }
            Map<UUID, InnerPromotionResponse> promoMap;
            promoMap = promotionFuture.join();
            
            return savedSkus.stream().map(
                sku -> {
                    InnerProductSkuResponse productSkuResponse = productSkuMapper.toInnerEntity(sku);
                    productSkuResponse.setPromotion(promoMap.getOrDefault(productSkuResponse.getId(), null));
                    return productSkuResponse;
                    
                }
            ).distinct().toList();
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [processVariantsOptimized] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void updateProductCache(ProductResponse productResponse) {
        try {
            // Set current timestamp as version
            productResponse.setVersion(System.currentTimeMillis());
            
            // Create cache key
            String cacheKey = this.productServiceCacheProperties.createCacheKey(
                this.productServiceCacheProperties.getKeys().getProductInfo(),
                productResponse.getId()
            );
            
            // Update both local cache and Redis
            cacheProvider.put(cacheKey, productResponse);
            
            log.info("PRODUCT-SERVICE: Updated cache for product ID: {}", productResponse.getId());
        } catch (Exception e) {
            // Don't fail the operation if cache update fails
            log.error("PRODUCT-SERVICE: [updateProductCache] Error updating cache: {}", e.getMessage(), e);
        }
    } 
}
