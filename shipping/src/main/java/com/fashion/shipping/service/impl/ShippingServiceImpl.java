package com.fashion.shipping.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.shipping.common.enums.EnumError;
import com.fashion.shipping.common.enums.ShippingEnum;
import com.fashion.shipping.common.enums.ShippingOperationEnum;
import com.fashion.shipping.common.response.ApiResponse;
import com.fashion.shipping.common.util.AsyncUtils;
import com.fashion.shipping.common.util.FormatTime;
import com.fashion.shipping.common.util.PageableUtils;
import com.fashion.shipping.common.util.SpecificationUtils;
import com.fashion.shipping.config.RedisClientConfig;
import com.fashion.shipping.dto.request.ShippingRequest;
import com.fashion.shipping.dto.request.search.SearchModel;
import com.fashion.shipping.dto.request.search.SearchOption;
import com.fashion.shipping.dto.request.search.SearchRequest;
import com.fashion.shipping.dto.response.PaginationResponse;
import com.fashion.shipping.dto.response.ShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerInternalShippingResponse;
import com.fashion.shipping.dto.response.ShippingResponse.InnerTempShippingFeeResponse;
import com.fashion.shipping.dto.response.internal.OrderResponse;
import com.fashion.shipping.dto.response.internal.PaymentResponse;
import com.fashion.shipping.dto.response.kafka.SagaStateResponse;
import com.fashion.shipping.entity.Shipping;
import com.fashion.shipping.exception.ServiceException;
import com.fashion.shipping.factory.ShippingProcessorFactory;
import com.fashion.shipping.integration.OrderClient;
import com.fashion.shipping.mapper.ShippingMapper;
import com.fashion.shipping.properties.cache.ShippingServiceGhnCacheProperties;
import com.fashion.shipping.repository.ShippingRepository;
import com.fashion.shipping.service.ShippingService;
import com.fashion.shipping.service.provider.CacheProvider;
import com.fashion.shipping.service.strategy.ShippingStrategy;
import com.google.common.cache.Cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class ShippingServiceImpl implements ShippingService{
    ShippingRepository shippingRepository;
    ShippingMapper shippingMapper;
    Executor virtualExecutor;
    CacheProvider cacheProvider;
    ShippingServiceGhnCacheProperties shippingServiceCacheProperties;
    ShippingProcessorFactory shippingProcessorFactory;
    OrderClient orderClient;

    @Override
    public ShippingResponse getShippingById(UUID id, String date, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, ShippingResponse.class, 
                () -> {
                    Instant[] range = FormatTime.getMonthRange(date);
                    Instant start = range[0];
                    Instant end = range[1];
                    return this.shippingRepository.findByIdInPartition(id,start, end)
                    .map(payment -> {
                        ShippingResponse shippingResponse = this.shippingMapper.toDto(payment);
                        shippingResponse.setVersion(System.currentTimeMillis());
                        return shippingResponse;
                    })
                    .orElse(null);
                }
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getShippingById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<ShippingResponse>> getAllShipping(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(Shipping.class);

            Specification<Shipping> spec = new SpecificationUtils<Shipping>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Shipping> shippings = this.shippingRepository.findAll(spec, pageRequest);
            List<ShippingResponse> shippingResponses = this.shippingMapper.toDto(shippings.getContent());
            return PageableUtils.<Shipping, ShippingResponse>buildPaginationResponse(pageRequest, shippings, shippingResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [getAllShipping] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public ShippingResponse createShipping(ShippingRequest request) {
        try {
            ShippingResponse response = this.processShipping(request, UUID.randomUUID(), ShippingOperationEnum.CREATE);
            this.updateShippingCache(response);
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [createShipping] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public ShippingResponse updateShipping(ShippingRequest request) {
        try {
            ShippingResponse response = this.processShipping(request, UUID.randomUUID(), ShippingOperationEnum.UPDATE);
            this.updateShippingCache(response);
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [updateShipping] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public SagaStateResponse commandShipping(ShippingResponse request, UUID eventId){
        try {
            if (shippingRepository.existsByEventId(eventId)) {
                log.warn("SHIPPING: Event {} already processed. Skipping.", eventId);
                return SagaStateResponse.success(request.getId(), ShippingEnum.PENDING);
            }

            if (request == null || request.getOrderId() == null) {
                return SagaStateResponse.failure(
                    null,
                    ShippingEnum.FAILED,
                    "Invalid shipping request"
                );
            }
            
            ShippingRequest shippingRequest = ShippingRequest.builder()
                .id(request.getId())
                .estimatedDate(request.getEstimatedDate())
                .provider(request.getProvider())
                .shippingFee(request.getShippingFee())
                .status(ShippingEnum.PENDING)
                // .eventId(eventId)
                .orderId(request.getOrderId())
                .orderCode(request.getOrderCode())
                .orderCreatedAt(request.getOrderCreatedAt())
                .version(request.getVersion())
                .build();

            ShippingResponse response = processShipping(
                shippingRequest,
                eventId,
                request.getId() != null ? ShippingOperationEnum.SAGA_UPDATE : ShippingOperationEnum.SAGA_CREATE
            );

            this.updateShippingCache(response);

            return SagaStateResponse.success(response.getId(), response.getStatus());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            return SagaStateResponse.failure(
                request != null ? request.getId() : null,
                ShippingEnum.FAILED,
                "Internal error: " + e.getMessage()
            );
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public SagaStateResponse compensateShipping(ShippingResponse request, UUID eventId) {
        try {
            // Check if shipping exists
            if (request.getId() == null) {
                log.info("SHIPPING: No shipping to compensate");
                return SagaStateResponse.success(null, null);
            }

            String orderDate = request.getOrderCode().substring(0, 8);
            Instant[] range = FormatTime.getMonthRange(orderDate);

            Optional<Shipping> existingOpt = this.shippingRepository.findByIdInPartition(
                request.getId(),
                range[0],
                range[1]
            );

            if (existingOpt.isEmpty()) {
                return SagaStateResponse.success(request.getId(), null);
            }

            Shipping shipping = existingOpt.get();
            
            shipping.setStatus(ShippingEnum.FAILED);
            shipping.setEventId(eventId);

            Shipping saved = shippingRepository.saveAndFlush(shipping);
            
            // Update cache
            ShippingResponse response = shippingMapper.toDto(saved);
            updateShippingCache(response);

            log.info("SHIPPING: Compensation completed for shipping {}", shipping.getId());
            
            return SagaStateResponse.success(saved.getId(), ShippingEnum.FAILED);

        } catch (Exception e) {
            return SagaStateResponse.failure(
                request != null ? request.getId() : null,
                ShippingEnum.FAILED,
                "Compensation failed: " + e.getMessage()
            );
        }
    }

    @Override
    public InnerTempShippingFeeResponse getThirdPartyShippingFee(InnerInternalShippingResponse request) {
        try {
            ShippingStrategy processor = this.shippingProcessorFactory.getProcessor(request.getProvider().toString());
            InnerTempShippingFeeResponse shippingFee = processor.shippingFee(request);
            return shippingFee;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [getThirdPartyShippingFee] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private ShippingResponse processShipping(
        ShippingRequest request,
        UUID eventId,
        ShippingOperationEnum operation
    ){
        try {
            Instant createdAt = Instant.now();
            String orderDate = request.getOrderCode().substring(0, 8);
            Instant[] range = FormatTime.getMonthRange(orderDate);
            Instant start = range[0];
            Instant end = range[1];

            boolean needOrderValidation = operation.needsOrderValidation();
            boolean needShippingFee = operation.needsShippingFeeCalculation();
            boolean isUpdate = operation.isUpdate();

            CompletableFuture<Optional<Shipping>> shippingFuture = isUpdate
                ? AsyncUtils.fetchAsyncWThread(
                    () -> shippingRepository.lockShippingById(request.getId(), start, end),
                    virtualExecutor
                )
                : CompletableFuture.completedFuture(Optional.empty());

            CompletableFuture<Void> duplicateCheckFuture = !isUpdate
                ? AsyncUtils.fetchVoidWThread(
                    () -> checkExistedShipping(request.getOrderId(), null, start, end),
                    virtualExecutor
                )
                : CompletableFuture.completedFuture(null);

            CompletableFuture<OrderResponse> orderFuture = needOrderValidation
                ? AsyncUtils.fetchAsyncWThread(
                    () -> orderClient.getInternalOrderById(
                        request.getOrderId(), 
                        request.getVersion()
                        ,
                        request.getOrderCode()
                    ).getData(),
                    virtualExecutor
                )
                : CompletableFuture.completedFuture(null);

            CompletableFuture<InnerTempShippingFeeResponse> feeFuture = CompletableFuture.completedFuture(null);

            if (needShippingFee) {
                // Wait for order first (needed for address)
                OrderResponse order = orderFuture.join();
                
                if (order != null && order.getId() != null) {
                    feeFuture = AsyncUtils.fetchAsyncWThread(() -> {
                        InnerInternalShippingResponse shippingReq = InnerInternalShippingResponse.builder()
                            .provider(request.getProvider())
                            .address(order.getReceiverAddress())
                            .district(order.getReceiverDistrict())
                            .province(order.getReceiverProvince())
                            .ward(order.getReceiverWard())
                            .version(request.getVersion())
                            .build();

                        ShippingStrategy processor = shippingProcessorFactory.getProcessor(
                            request.getProvider().toString()
                        );
                        
                        return processor.shippingFee(shippingReq);
                    }, virtualExecutor);
                }
            }

            try {
                CompletableFuture.allOf(
                    shippingFuture,
                    duplicateCheckFuture,
                    orderFuture,
                    feeFuture
                ).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException se) {
                    throw se;
                }
                throw e;
            }

            Optional<Shipping> existingOpt = shippingFuture.join();
            OrderResponse order = orderFuture.join();
            InnerTempShippingFeeResponse feeResponse = feeFuture.join();

            if (needOrderValidation && (order == null || order.getId() == null)) {
                throw new ServiceException(
                    EnumError.SHIPPING_SHIPPING_ERR_NOT_FOUND_ORDER_ID,
                    "shipping.not.found.orderId"
                );
            }
            Shipping shipping;
        
            if (isUpdate) {
                // Update existing
                shipping = existingOpt.orElseThrow(() -> new ServiceException(
                    EnumError.SHIPPING_SHIPPING_ERR_NOT_FOUND_ID,
                    "shipping.not.found.id",
                    Map.of("id", request.getId())
                ));
                
                shipping.setStatus(request.getStatus() != null ? request.getStatus() : shipping.getStatus());
                shipping.setUpdatedAt(createdAt);
                
            } else {
                // Create new
                shipping = new Shipping();
                shipping.setId(UUID.randomUUID());
                shipping.setStatus(request.getStatus() != null ? request.getStatus() : ShippingEnum.PENDING);
                shipping.setCreatedAt(createdAt);
                shipping.setTrackingCode(UUID.randomUUID().toString());
            }

            // Set common fields
            shipping.setActivated(true);
            shipping.setProvider(request.getProvider());
            shipping.setOrderId(request.getOrderId());
            shipping.setOrderCode(request.getOrderCode());
            shipping.setOrderCreatedAt(request.getOrderCreatedAt());
            shipping.setEventId(eventId);

            // Set fee and estimated date
            if (needShippingFee && feeResponse != null) {
                shipping.setShippingFee(feeResponse.getShippingFee());
                shipping.setEstimatedDate(feeResponse.getEstimatedDate());
            } else {
                shipping.setShippingFee(request.getShippingFee());
                shipping.setEstimatedDate(request.getEstimatedDate());
            }

            // ✅ STEP 8: Save
            Shipping saved = shippingRepository.saveAndFlush(shipping);
            
            log.info("SHIPPING: {} completed for shipping {}", operation, saved.getId());
            
            return shippingMapper.toDto(saved);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [upSertShipping] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    private Void checkExistedShipping(UUID orderId, UUID excludedId, Instant start, Instant end){
        try {
            Optional<Shipping> optional;
            if(excludedId == null){
                optional = this.shippingRepository.findDuplicateForCreate(orderId, start, end);
            } else {
                optional = this.shippingRepository.findDuplicateForUpdate(orderId, excludedId, start, end);
            }
            optional.ifPresent(p -> {
                throw new ServiceException(
                    EnumError.SHIPPING_SHIPPING_ERR_NOT_FOUND_ID, 
                    "shipping.exist.orderId",
                    Map.of("orderId", orderId
                ));
            });
            return null;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkExistedShipping] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.SHIPPING_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private String getCacheKey(UUID id){
        return this.shippingServiceCacheProperties.createCacheKey(
            this.shippingServiceCacheProperties.getKeys().getShippingInfo(),
            id
        );
    }

    private String getLockKey(UUID id){
        return this.shippingServiceCacheProperties.createLockKey(
            this.shippingServiceCacheProperties.getKeys().getShippingInfo(),
            id
        );
    }

    private void updateShippingCache(ShippingResponse shippingResponse) {
        try {
            shippingResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(shippingResponse.getId());
            cacheProvider.put(cacheKey, shippingResponse);
            
            log.info("SHIPPING-SERVICE: Updated cache for shipping ID: {}", shippingResponse.getId());
        } catch (Exception e) {
            log.error("SHIPPING-SERVICE: [updateShippingCache] Error updating cache: {}", e.getMessage(), e);
        }
    }


}
