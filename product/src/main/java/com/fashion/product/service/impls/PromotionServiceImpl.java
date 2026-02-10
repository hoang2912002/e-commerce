package com.fashion.product.service.impls;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.ServerException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.enums.PromotionEnum;
import com.fashion.product.common.util.ConvertUuidUtil;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.PromotionRequest;
import com.fashion.product.dto.request.CategoryRequest.InnerCategoryRequest;
import com.fashion.product.dto.request.ProductRequest.InnerProductRequest;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.CategoryResponse.InnerCategoryResponse;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.ProductResponse.InnerProductResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.dto.response.PromotionResponse.InnerPromotionResponse;
import com.fashion.product.entity.Category;
import com.fashion.product.entity.OptionValue;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.entity.Promotion;
import com.fashion.product.entity.PromotionProduct;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.CategoryMapper;
import com.fashion.product.mapper.ProductMapper;
import com.fashion.product.mapper.PromotionMapper;
import com.fashion.product.repository.ProductRepository;
import com.fashion.product.repository.ProductSkuRepository;
import com.fashion.product.repository.PromotionProductRepository;
import com.fashion.product.repository.PromotionRepository;
import com.fashion.product.service.CategoryService;
import com.fashion.product.service.ProductService;
import com.fashion.product.service.PromotionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionServiceImpl implements PromotionService{
    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;
    CategoryService categoryService;
    ProductRepository productRepository;
    PromotionProductRepository promotionProductRepository;
    CategoryMapper categoryMapper;
    ProductMapper productMapper;
    ProductSkuRepository productSkuRepository;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PromotionResponse createPromotion(
        PromotionRequest request
    ) {
        try {
            return this.upSertPromotion(new Promotion(), request, request.getCategories(), request.getProducts());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createPromotion] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
        
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PromotionResponse updatePromotion(PromotionRequest request) {
        try {
            Promotion promotion = this.lockPromotionById(request.getId());
            return this.upSertPromotion(promotion, request, request.getCategories(), request.getProducts());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createPromotion] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(UUID id) {
        try {
            Promotion promotion = this.promotionRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.PRODUCT_PROMOTION_ERR_NOT_FOUND_ID, 
                    "promotion.not.found.id",
                    Map.of("id", id))
            );
            final List<InnerCategoryResponse> pCategories = promotion.getPromotionProducts().stream()
                .map(PromotionProduct::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Category::getId, c -> categoryMapper.toInnerEntity(c), (a, b) -> a))
                .values()
                .stream()
                .toList();

            final List<InnerProductResponse> pProducts = promotion.getPromotionProducts().stream()
                .map(PromotionProduct::getProduct)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Product::getId, p -> productMapper.toInnerEntity(p), (a, b) -> a))
                .values()
                .stream()
                .toList();
            final PromotionResponse promotionDTO = promotionMapper.toDto(promotion);
            promotionDTO.setCategories(pCategories);
            promotionDTO.setProducts(pProducts);
            return promotionDTO;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createPromotion] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<PromotionResponse>> getAllPromotion(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(Promotion.class);
            Specification<Promotion> spec = new SpecificationUtils<Promotion>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Promotion> promotions = this.promotionRepository.findAll(spec, pageRequest);
            List<PromotionResponse> promotionResponses = this.promotionMapper.toDto(promotions.getContent());
            return PageableUtils.<Promotion, PromotionResponse>buildPaginationResponse(pageRequest, promotions, promotionResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllPromotion] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InnerPromotionResponse getInternalCorrespondingPromotionByProductId(ProductSku productSku) {
        try {
            BigDecimal productPrice = productSku.getPrice();
            List<Promotion> promotions = this.promotionProductRepository
                .findAllByProductId(productSku.getProduct().getId())
                .stream()
                .map(PromotionProduct::getPromotion)
                .filter(Objects::nonNull)
                .filter(p -> !p.getEndDate().isBefore(LocalDate.now()))  // còn hiệu lực
                .distinct()
                .toList();
            
            InnerPromotionResponse best = null;
            BigDecimal maxDiscount = BigDecimal.ZERO;

            for (Promotion p : promotions) {
                BigDecimal discount = calculateDiscountValue(p, productPrice);
                if (discount.compareTo(maxDiscount) > 0) {
                    maxDiscount = discount;
                    best = promotionMapper.toInnerEntity(p);
                    best.setDiscountFinal(maxDiscount);
                }
            }
            return best;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getInternalCorrespondingPromotionByProductId] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        } 
    }

    @Override
    @Transactional(rollbackFor = ServerException.class)
    public Promotion lockPromotionById(UUID id){
        return this.promotionRepository.lockPromotionById(id);
    }

    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void spinningQuantity(Map<UUID, Integer> productSkus, UUID eventId) {
        try {
            if (this.promotionRepository.existsByEventId(eventId)) {
                log.warn("PRODUCT-SERVICE: Event {} already processed. Skipping.", eventId);
                return; 
            }
            Map<UUID, ProductSku> skus = this.productSkuRepository.findAllByIdIn(productSkus.keySet()).stream().collect(Collectors.toMap(ProductSku::getId, Function.identity()));
            for (Map.Entry<UUID, Integer> pSku : productSkus.entrySet()) {
                ProductSku productSku = skus.getOrDefault(pSku.getKey(), null);
                if(productSku != null){
                    InnerPromotionResponse promotion = this.getInternalCorrespondingPromotionByProductId(productSku);
                    int updatedRows;
                    if(pSku.getValue() > 0){
                        updatedRows = this.promotionRepository.increaseQuantityAtomic(
                            promotion.getId(),
                            Math.abs(pSku.getValue()),
                            eventId
                        );
                    } else {
                        updatedRows = this.promotionRepository.decreaseQuantityAtomic(
                            promotion.getId(),
                            Math.abs(pSku.getValue()),
                            eventId
                        );
                    }
                    if (updatedRows == 0) {
                        throw new ServiceException(
                            EnumError.PRODUCT_PROMOTION_INVALID_QUANTITY, 
                            "promotion.quantity.stock.error.for.atomic.update"
                        );
                    }  
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [spinningQuantity] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        } 
    }

    @Override
    public Map<UUID, InnerPromotionResponse> getBestPromotionsByProductIds(Collection<ProductSku> productSkus, UUID pId) {
        if (pId == null) return Collections.emptyMap();
        try {
            List<PromotionProduct> promotionProducts = promotionProductRepository.findAllByProductId(pId);
            if(promotionProducts.size() <= 0) return new HashMap<>(); 
            return productSkus.stream().collect(Collectors.toMap(
                ProductSku::getId,
                sku -> findBestPromotion(sku, promotionProducts),
                (a, b) -> a
            ));
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private InnerPromotionResponse findBestPromotion(ProductSku sku, List<PromotionProduct> promotions) {
        InnerPromotionResponse best = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (PromotionProduct item : promotions) {
            BigDecimal discount = calculateDiscountValue(item.getPromotion(), sku.getPrice());
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                best = promotionMapper.toInnerEntity(item.getPromotion());
                best.setDiscountFinal(maxDiscount);
            }
        }
        return best;
    }

    private PromotionResponse upSertPromotion(
        Promotion promotion, 
        PromotionRequest promotionRequest,
        List<InnerCategoryRequest> categoryRequests,
        List<InnerProductRequest> productRequests
    ) {
        try {
            this.checkPromotionExistByCode(promotion.getCode(), promotion.getId());
            final boolean isAmountType = promotionRequest.getOptionPromotion() == 1;

            promotionMapper.toUpdate(promotion, promotionRequest);
            
            promotion.setActivated(true);
            promotion.setDiscountPercent(isAmountType ? null : promotionRequest.getDiscountPercent());
            promotion.setMinDiscountAmount(isAmountType ? promotionRequest.getMinDiscountAmount() : null);
            promotion.setMaxDiscountAmount(isAmountType ? promotionRequest.getMaxDiscountAmount() : null);
            if(promotion.getEventId() == null){
                promotion.setEventId(UUID.randomUUID());
            }

            List<PromotionProduct> newItems = new ArrayList<>();
            
            if (PromotionEnum.CATEGORY.equals(promotion.getDiscountType()) && categoryRequests != null) {
                List<UUID> cateIds = categoryRequests.stream().map(InnerCategoryRequest::getId).distinct().toList();
                List<Category> allCategories = this.categoryService.getCategoryTreeStartByListId(cateIds);
                
                newItems = allCategories.stream()
                    .flatMap(c -> c.getProducts().stream()
                        .map(p -> PromotionProduct.builder()
                            .category(c)
                            .product(p)
                            .promotion(promotion) // Dùng biến promotion gốc đã Lock
                            .activated(true)
                            .build()
                        )
                    ).collect(Collectors.toList());
            } else if (productRequests != null) {
                List<UUID> productIds = productRequests.stream().map(InnerProductRequest::getId).distinct().toList();
                List<Product> products = this.productRepository.findAllByIdIn(productIds);
                
                newItems = products.stream()
                    .map(p -> PromotionProduct.builder()
                        .category(p.getCategory())
                        .product(p)
                        .promotion(promotion) // Dùng biến promotion gốc đã Lock
                        .activated(true)
                        .build()
                    ).collect(Collectors.toList());
            }

            if (promotion.getPromotionProducts() == null) {
                promotion.setPromotionProducts(new ArrayList<>());
            }
            
            promotion.getPromotionProducts().clear();
            if (!newItems.isEmpty()) {
                promotion.getPromotionProducts().addAll(newItems);
            }

            Promotion savedPromotion = this.promotionRepository.save(promotion);
            return buildPromotionResponse(savedPromotion);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [upSertPromotion] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    private PromotionResponse buildPromotionResponse(Promotion savedPromotion) {
        final List<InnerCategoryResponse> cateResponse = savedPromotion.getPromotionProducts().stream()
            .map(PromotionProduct::getCategory)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Category::getId, categoryMapper::toInnerEntity, (a, b) -> a))
            .values().stream().toList();

        final List<InnerProductResponse> productResponse = savedPromotion.getPromotionProducts().stream()
            .map(PromotionProduct::getProduct)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Product::getId, productMapper::toInnerEntity, (a, b) -> a))
            .values().stream().toList();

        PromotionResponse response = promotionMapper.toDto(savedPromotion);
        response.setCategories(cateResponse);
        response.setProducts(productResponse);
        return response;
    }
    private void checkPromotionExistByCode(String code, UUID excludeId){
        try {
            Optional<Promotion> promotion;
            if(Objects.isNull(excludeId)){
                promotion = this.promotionRepository.findDuplicateForCreate(code);
            } else {
                promotion = this.promotionRepository.findDuplicateForUpdate(code,excludeId);
            }
            promotion.ifPresent(user -> {
                throw new ServiceException(
                    EnumError.PRODUCT_PROMOTION_DATA_EXISTED_CODE, 
                    "promotion.exist.code",
                    Map.of("code", code));
            });
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkPromotionExistByCode] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private BigDecimal calculateDiscountValue(Promotion p, BigDecimal productPrice) {
        BigDecimal discount = BigDecimal.ZERO;

        if (p == null || productPrice == null) {
            return BigDecimal.ZERO;
        }

        // ========== CASE 1: GIẢM THEO % (Option = 0) ==========
        if (p.getOptionPromotion() == 0) {
            if (p.getDiscountPercent() != null) {
                discount = productPrice.multiply(BigDecimal.valueOf(p.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP); // Làm tròn số tiền (thường là 0 số thập phân với VNĐ)
            }
        } 
        // ========== CASE 2: GIẢM THEO SỐ TIỀN CỐ ĐỊNH ==========
        else {
            if (p.getMinDiscountAmount() != null) {
                discount = p.getMinDiscountAmount();
            }
        }

        // ========== ÁP DỤNG MIN/MAX CHO CẢ HAI TRƯỜNG HỢP ==========
        // 1. Kiểm tra Min Discount (Giảm giá tối thiểu)
        if (p.getMinDiscountAmount() != null) {
            discount = discount.max(p.getMinDiscountAmount());
        }

        // 2. Kiểm tra Max Discount (Giảm giá tối đa - Cap)
        if (p.getMaxDiscountAmount() != null) {
            discount = discount.min(p.getMaxDiscountAmount());
        }

        // ========== LOGIC AN TOÀN GIÁ (CRITICAL) ==========
        // Không cho giảm > giá sản phẩm 
        if (discount.compareTo(productPrice) > 0) {
            discount = productPrice;
        }

        return discount;
    }
}
