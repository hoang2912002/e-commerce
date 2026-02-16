package com.fashion.payment.service.impl;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import javax.swing.Spring;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.payment.common.enums.EnumError;
import com.fashion.payment.common.enums.PaymentEnum;
import com.fashion.payment.common.enums.PaymentMethodEnum;
import com.fashion.payment.common.response.ApiResponse;
import com.fashion.payment.common.util.AsyncUtils;
import com.fashion.payment.common.util.FormatTime;
import com.fashion.payment.common.util.PageableUtils;
import com.fashion.payment.common.util.SpecificationUtils;
import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.request.PaymentMethodRequest.InnerPaymentMethodRequest;
import com.fashion.payment.dto.request.search.SearchModel;
import com.fashion.payment.dto.request.search.SearchOption;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;
import com.fashion.payment.dto.response.internal.InventoryResponse;
import com.fashion.payment.dto.response.internal.OrderResponse;
import com.fashion.payment.dto.response.kafka.SagaStateResponse;
import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentMethod;
import com.fashion.payment.entity.PaymentTransaction;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.factory.PaymentProcessorFactory;
import com.fashion.payment.integration.OrderClient;
import com.fashion.payment.mapper.PaymentMapper;
import com.fashion.payment.properties.PaymentCodProperties;
import com.fashion.payment.properties.cache.PaymentServiceCacheProperties;
import com.fashion.payment.repository.PaymentMethodRepository;
import com.fashion.payment.repository.PaymentRepository;
import com.fashion.payment.repository.PaymentTransactionRepository;
import com.fashion.payment.service.PaymentMethodService;
import com.fashion.payment.service.PaymentService;
import com.fashion.payment.service.provider.CacheProvider;
import com.fashion.payment.service.strategy.PaymentStrategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy.SelfInjection.Split;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService{
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
    OrderClient orderClient;
    PaymentMethodRepository paymentMethodRepository;
    PaymentProcessorFactory paymentProcessorFactory;
    PaymentCodProperties paymentCodProperties;
    PaymentTransactionRepository paymentTransactionRepository;
    PaymentMethodService paymentMethodService;
    PaymentServiceCacheProperties paymentServiceCacheProperties;
    Executor virtualExecutor;
    CacheProvider cacheProvider;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("PAYMENT-SERVICE: [createPayment] Start create payment");
        try {
            PaymentResponse paymentResponse = this.savePayment(request, UUID.randomUUID(), false, true);
            this.updatePaymentCache(paymentResponse);
            return paymentResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [createPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PaymentResponse updatePayment(PaymentRequest request) {
        log.info("PAYMENT-SERVICE: [updatePayment] Start update payment");
        try {
            PaymentResponse paymentResponse = this.savePayment(request, UUID.randomUUID(), false, true);
            this.updatePaymentCache(paymentResponse);
            return paymentResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [updatePayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    public PaymentResponse getPaymentById(UUID id, String date, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, PaymentResponse.class, 
                () -> {
                    Instant[] range = FormatTime.getMonthRange(date);
                    Instant start = range[0];
                    Instant end = range[1];
                    return this.paymentRepository.findByIdInPartition(id,start, end)
                    .map(payment -> {
                        PaymentResponse paymentResponse = this.paymentMapper.toDto(payment);
                        paymentResponse.setVersion(System.currentTimeMillis());
                        return paymentResponse;
                    })
                    .orElse(null);
                }
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [getPaymentById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<PaymentResponse>> getAllPayment(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(Payment.class);

            Specification<Payment> spec = new SpecificationUtils<Payment>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Payment> payments = this.paymentRepository.findAll(spec, pageRequest);
            List<PaymentResponse> paymentResponses = this.paymentMapper.toDto(payments.getContent());
            return PageableUtils.<Payment, PaymentResponse>buildPaginationResponse(pageRequest, payments, paymentResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [getAllPaymentMethod] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    public void deletePaymentById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePaymentById'");
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public SagaStateResponse upSertPayment(InnerInternalPayment request, UUID eventId, Boolean isSuccess) {
        try {
            if(!this.checkExistedDataPayment(request)) 
                return SagaStateResponse.failurePayment(request.getId(), request.getStatus(), "Payment data from Order must not be null"); 
            if(this.paymentTransactionRepository.existsByEventId(eventId)){
                log.warn("PAYMENT-SERVICE: Event {} already processed. Skipping.", eventId);
                return SagaStateResponse.failurePayment(request.getId(), request.getStatus(), "Payment already existed with the eventId");             
            }
            InnerPaymentMethodRequest pMethodRequest = InnerPaymentMethodRequest.builder()
                .code(request.getPaymentMethod())
                .build();
            PaymentRequest paymentRequest = PaymentRequest.builder()
                .id(request.getId())
                .amount(request.getAmount())
                .orderCode(request.getOrderCode())
                .status(request.getStatus())
                .orderId(request.getOrderId())
                .paymentMethod(pMethodRequest)
                .orderCreatedAt(request.getOrderCreatedAt())
                .build();
            PaymentResponse paymentResponse = this.savePayment(paymentRequest,eventId,true, isSuccess);
            return SagaStateResponse.successPayment(paymentResponse.getId(), paymentResponse.getStatus());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [UpSertPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        } 
    }
    /**
     * @checkPayment received from kafka
     * @return boolean
     */
    private boolean checkExistedDataPayment(InnerInternalPayment payment){
        try {
            for (Field field : InnerInternalPayment.class.getDeclaredFields()) {
                if(field.getName().contains("id")) continue;
                field.setAccessible(true);
                Object dataPayment = field.get(payment);
                if(dataPayment == null) return false;
            }
            return true;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [checkExistedDataPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    private PaymentResponse savePayment(
        PaymentRequest request, 
        UUID eventId, 
        boolean skipCheckOrder, 
        Boolean isSuccess // Saga Orchestration
    ) {
        try {
            boolean isUpdate = request.getId() != null;

            // Extract partition range
            Instant createdAt = Instant.now();
            String orderDate = request.getOrderCode().substring(0, 8);
            Instant[] range = FormatTime.getMonthRange(orderDate);
            Instant start = range[0];
            Instant end = range[1];

            CompletableFuture<Optional<Payment>> paymentFuture;
            if (isUpdate) {
                paymentFuture = AsyncUtils.fetchAsyncWThread(
                    () -> this.paymentRepository.lockPaymentById(request.getId(), start, end), virtualExecutor);
            } else {
                paymentFuture = CompletableFuture.supplyAsync(() -> Optional.of(new Payment()));
            }
            CompletableFuture<Void> isExistedPayFuture = AsyncUtils.fetchVoidWThread(
                () -> this.checkExistedPayment(request.getOrderId(), request.getId(), start, end), virtualExecutor);
            
            CompletableFuture<PaymentMethod> paymentMethFuture = AsyncUtils.fetchAsyncWThread(
                () -> this.paymentMethodService.getPaymentMethodByIdOrCode(request.getPaymentMethod().getId(), request.getPaymentMethod().getCode()), virtualExecutor);
            
            CompletableFuture<ApiResponse<OrderResponse>> orderFuture = CompletableFuture.completedFuture(new ApiResponse<>());
            
            if(!skipCheckOrder){
                orderFuture = AsyncUtils.fetchAsyncWThread(() -> this.orderClient.getInternalOrderById(request.getOrderId(), request.getVersion()), virtualExecutor);
            }

            try {
                CompletableFuture.allOf(paymentFuture,isExistedPayFuture,paymentMethFuture,orderFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }

            Payment payment = paymentFuture.join().orElseThrow(
                () -> new ServiceException(EnumError.PAYMENT_PAYMENT_ERR_NOT_FOUND_ID, "payment.not.found.id", Map.of("id", request.getId()))
            );
            isExistedPayFuture.join();
            PaymentMethod paymentMethod = paymentMethFuture.join();
            OrderResponse orderResponse = orderFuture.join().getData();

            // Update position if from saga order failed => isSuccess = false
            if(isUpdate){
                if(isSuccess == true){
                    if (List.of(PaymentEnum.FAILED, PaymentEnum.SUCCESS).contains(payment.getStatus()))
                        throw new ServiceException(EnumError.PAYMENT_PAYMENT_STATUS_NOT_PENDING_CANNOT_UPDATE, "payment.status.cannot.update.payment", Map.of("id", request.getId()));

                }
            } else{
                payment.setStatus(PaymentEnum.PENDING);
                payment.setId(UUID.randomUUID());
                payment.setCreatedAt(createdAt);
            }
            
            if(paymentMethod.getStatus() != PaymentMethodEnum.ACTIVE)
                throw new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_STATUS_NOT_MATCHING, "payment.method.status.not.support.current");

            payment.setActivated(true);
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(orderResponse != null ? orderResponse.getFinalPrice() : request.getAmount()); 
            payment.setOrderId(orderResponse != null ? orderResponse.getId() : request.getOrderId());
            payment.setOrderCode(request.getOrderCode());
            payment.setOrderCreatedAt(request.getOrderCreatedAt());

            Payment savedPayment = paymentRepository.save(payment);
            paymentRepository.flush(); 

            String action = isUpdate ? "UPDATE" : "INIT";
            String transactionId = String.join("_", paymentMethod.getCode().toUpperCase(), action, eventId.toString());

            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .eventId(eventId)
                .transactionId(transactionId)
                .status(PaymentEnum.PENDING)
                .payment(payment) 
                .paymentId(savedPayment.getId())
                .paymentCreatedAt(savedPayment.getCreatedAt())
                .note(isUpdate ? "User changed payment method" : "Initial payment request")
                .createdAt(createdAt)
                .build();
            
            PaymentTransaction saveTransaction = this.paymentTransactionRepository.save(paymentTransaction);
            PaymentResponse paymentResponse;
            if (!paymentMethod.getCode().equalsIgnoreCase(this.paymentCodProperties.getPartnerCode()) && isSuccess) {
                PaymentStrategy paymentStrategy = this.paymentProcessorFactory.getProcessor(paymentMethod.getCode().toLowerCase());
                paymentResponse = paymentStrategy.process(savedPayment, eventId);
            } else {
                paymentResponse = this.paymentMapper.toDto(savedPayment);
            }

            return paymentResponse;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [savePayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkExistedPayment(UUID orderId, UUID excludedId, Instant start, Instant end){
        try {
            Optional<Payment> optional;
            if(excludedId == null){
                optional = this.paymentRepository.findDuplicateForCreate(orderId, start, end);
            } else {
                optional = this.paymentRepository.findDuplicateForUpdate(orderId, excludedId, start, end);
            }
            optional.ifPresent(p -> {
                throw new ServiceException(
                    EnumError.PAYMENT_PAYMENT_ERR_NOT_FOUND_ORDER_ID, 
                    "payment.exist.orderId",
                    Map.of("orderId", orderId
                ));
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkExistedPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private String getCacheKey(UUID id){
        return this.paymentServiceCacheProperties.createCacheKey(
            this.paymentServiceCacheProperties.getKeys().getPaymentInfo(),
            id
        );
    }

    private String getLockKey(UUID id){
        return this.paymentServiceCacheProperties.createLockKey(
            this.paymentServiceCacheProperties.getKeys().getPaymentInfo(),
            id
        );
    }

    private void updatePaymentCache(PaymentResponse paymentResponse) {
        try {
            paymentResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(paymentResponse.getId());
            cacheProvider.put(cacheKey, paymentResponse);
            
            log.info("PAYMENT-SERVICE: Updated cache for payment ID: {}", paymentResponse.getId());
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [updatePaymentCache] Error updating cache: {}", e.getMessage(), e);
        }
    }
}
