package com.fashion.product.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SlugUtil;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Category;
import com.fashion.product.entity.OptionValue;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.entity.ShopManagement;
import com.fashion.product.entity.Variant;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.ProductMapper;
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
    CategoryRepository categoryRepository;
    CategoryService categoryService;
    ProductMapper productMapper;
    ProductSkuRepository productSkuRepository;
    VariantRepository variantRepository;
    ProductSkuService productSkuService;
    OptionValueRepository optionValueRepository;
    ApprovalHistoryService approvalHistoryService;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("PRODUCT-SERVICE: [createProduct] Start create product");
        try {
            Product product = this.productMapper.toValidated(request);
            return upSertProduct(product,request.getVariants(),request.getCategory().getId(),request.getShopManagement().getId());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public ProductResponse updateProduct(ProductRequest request) {
        log.info("PRODUCT-SERVICE: [updateProduct] Start update product");
        try {
            Product product = this.productRepository.lockProductById(request.getId()).orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,"product.not.found.id", Map.of("id",request.getId()))
            );
            this.productMapper.toUpdate(product, request);
            
            product.getVariants().clear();
            this.variantRepository.deleteAllByProductId(product.getId());

            return upSertProduct(product,request.getVariants(),request.getCategory().getId(),request.getShopManagement().getId());
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
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        try {
            Product product = this.productRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.PRODUCT_PRODUCT_ERR_NOT_FOUND_ID,"product.not.found.id", Map.of("id", id))
            );
            return this.productMapper.toDto(product);
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

    private ProductResponse upSertProduct(Product product, List<InnerVariantRequest> variants, UUID categoryId, UUID shopManagementId){
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

            this.approvalHistoryService.handleApprovalHistoryUpSertProduct(createdProduct,isCreate,ApprovalHistoryServiceImpl.ENTITY_TYPE_PRODUCT);
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
            Optional<Product> optional;
            if(excludedId == null){
                optional = this.productRepository.findDuplicateForCreate(slug, cateId, smId);
            } else {
                optional = this.productRepository.findDuplicateForUpdate(slug, cateId, smId, excludedId);
            }
            optional.ifPresent(p -> {
                throw new ServiceException(
                    EnumError.PRODUCT_PRODUCT_DATA_EXISTED_NAME, 
                    "product.exist.name",
                    Map.of("name", name
                ));
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkExistedProduct] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
