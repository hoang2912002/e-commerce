package com.fashion.product.service.impls;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.dto.request.VariantRequest.InnerVariantRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Product;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.repository.ProductRepository;
import com.fashion.product.service.ProductService;

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
    
    @Override
    public ProductResponse createProduct(Product product, List<InnerVariantRequest> variants) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createProduct'");
    }

    @Override
    public ProductResponse updateProduct(Product product, List<InnerVariantRequest> variants) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProduct'");
    }

    @Override
    public PaginationResponse getAllProduct() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllProduct'");
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProductById'");
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
    public Product lockProductById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'lockProductById'");
    }

    @Override
    public Product findRawProductById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawProductById'");
    }
    
}
