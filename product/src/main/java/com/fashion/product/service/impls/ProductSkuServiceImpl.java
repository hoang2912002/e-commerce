package com.fashion.product.service.impls;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fashion.product.entity.Product;
import com.fashion.product.service.ProductSkuService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductSkuServiceImpl implements ProductSkuService{


    @Override
    public void deleteProductSkuByListId(List<Long> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteProductSkuByListId'");
    }

    @Override
    public void validateAndMapSkuToInventoryRequests(Product product) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateAndMapSkuToInventoryRequests'");
    }
    
}
