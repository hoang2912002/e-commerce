package com.fashion.product.service.impls;

import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.enums.PromotionEnum;
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
    ProductService productService;
    PromotionProductRepository promotionProductRepository;
    CategoryMapper categoryMapper;
    ProductMapper productMapper;

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
    public PromotionResponse getCorrespondingPromotionByProductId(ProductSku productSku) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCorrespondingPromotionByProductId'");
    }

    @Override
    public void decreaseQuantity(UUID id, Integer quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decreaseQuantity'");
    }

    @Override
    public void increaseQuantity(UUID id, Integer quantity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'increaseQuantity'");
    }

    @Override
    @Transactional(rollbackFor = ServerException.class)
    public Promotion lockPromotionById(UUID id){
        return this.promotionRepository.lockPromotionById(id);
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
                List<Product> products = this.productService.findListProductById(productIds);
                
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

            Promotion savedPromotion = this.promotionRepository.saveAndFlush(promotion);
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
}
