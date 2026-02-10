package com.fashion.product.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.dto.response.ProductSkuResponse;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent;
import com.fashion.product.dto.response.kafka.ProductApprovedEvent.InternalProductApprovedEvent;
import com.fashion.product.entity.Product;
import com.fashion.product.entity.ProductSku;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.mapper.ProductSkuMapper;
import com.fashion.product.messaging.provider.ProductServiceProvider;
import com.fashion.product.repository.ProductSkuRepository;
import com.fashion.product.service.KafkaService;
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
    ProductSkuRepository productSkuRepository;
    ProductServiceProvider productServiceProvider;
    ApplicationEventPublisher applicationEventPublisher;
    ProductSkuMapper productSkuMapper;

    @Override
    public void deleteProductSkuByListId(List<Long> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteProductSkuByListId'");
    }

    @Override
    // @Transactional(rollbackFor = ServiceException.class)
    public void validateAndMapSkuToInventoryRequests(Product product) {
        try {
            this.productSkuRepository.lockSkuByProduct(product.getId());
            List<ProductSku> skus = product.getProductSkus();
            List<ProductApprovedEvent> eventPayload = new ArrayList<>();
            for (ProductSku sku : skus) {
                if (sku.getTempStock() != null && sku.getTempStock() > 0) {
                    eventPayload.add(ProductApprovedEvent.builder()
                        .productSkuId(sku.getId())
                        .productId(product.getId())
                        .quantityAvailable(sku.getTempStock())
                        .build());
                    
                    sku.setTempStock(0); // Reset ngay sau khi lấy giá trị
                }
                
            }

            if (!eventPayload.isEmpty()) {
                // this.productServiceProvider.produceProductApprovedEventSuccess(inventories);
                applicationEventPublisher.publishEvent(new InternalProductApprovedEvent(this, eventPayload));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [validateAndMapSkuToInventoryRequests] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductSkuResponse> getInternalProductSkuByIds(List<UUID> ids){
        try {
            List<ProductSku> productSku = this.productSkuRepository.findAllById(ids);
            return this.productSkuMapper.toDto(productSku);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getInternalProductSkuByIds] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
