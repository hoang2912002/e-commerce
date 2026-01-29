package com.fashion.payment.service.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
import com.fashion.payment.common.util.PageableUtils;
import com.fashion.payment.common.util.SpecificationUtils;
import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.request.search.SearchModel;
import com.fashion.payment.dto.request.search.SearchOption;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;
import com.fashion.payment.dto.response.internal.OrderResponse;
import com.fashion.payment.entity.Payment;
import com.fashion.payment.entity.PaymentMethod;
import com.fashion.payment.entity.PaymentTransaction;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.factory.PaymentProcessorFactory;
import com.fashion.payment.integration.OrderClient;
import com.fashion.payment.mapper.PaymentMapper;
import com.fashion.payment.properties.PaymentCodProperties;
import com.fashion.payment.repository.PaymentMethodRepository;
import com.fashion.payment.repository.PaymentRepository;
import com.fashion.payment.service.PaymentService;
import com.fashion.payment.service.strategy.PaymentStrategy;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void UpSertPayment(InnerInternalPayment payment, UUID eventId) {
        try {

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [UpSertPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        } 
    }
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("PAYMENT-SERVICE: [createPayment] Start create payment");
        try {
            return this.upSertPayment(request);
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
            return this.upSertPayment(request);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [updatePayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID id) {
        try {
            Payment payment = this.paymentRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.PAYMENT_PAYMENT_ERR_NOT_FOUND_ID, "payment.not.found.id", Map.of("id", id))
            );
            return this.paymentMapper.toDto(payment);
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
    
    private PaymentResponse upSertPayment(PaymentRequest request) {
        try {
            Payment payment;
            boolean isUpdate = request.getId() != null;

            if (isUpdate) {
                payment = this.paymentRepository.lockPaymentById(request.getId()).orElseThrow(
                    () -> new ServiceException(EnumError.PAYMENT_PAYMENT_ERR_NOT_FOUND_ID, "payment.not.found.id", Map.of("id", request.getId()))
                );
                
                if (List.of(PaymentEnum.FAILED, PaymentEnum.SUCCESS).contains(payment.getStatus())) {
                    throw new ServiceException(EnumError.PAYMENT_PAYMENT_STATUS_NOT_PENDING_CANNOT_UPDATE, "payment.status.cannot.update.payment", Map.of("id", request.getId()));
                }
            } else {
                payment = new Payment();
                payment.setPaymentTransactions(new ArrayList<>());
                payment.setStatus(PaymentEnum.PENDING);
            }
            this.checkExistedPayment(request.getOrderId(), request.getId());
            ApiResponse<OrderResponse> apiRes = this.orderClient.getInternalOrderById(request.getOrderId());
            OrderResponse orderResponse = apiRes.getData();
            
            PaymentMethod paymentMethod = this.paymentMethodRepository.findById(request.getPaymentMethod().getId()).orElseThrow(
                () -> new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_ERR_NOT_FOUND_ID, "payment.not.found.id", Map.of("paymentMethodId", request.getPaymentMethod().getId()))
            );
            if(paymentMethod.getStatus() != PaymentMethodEnum.ACTIVE){
                throw new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_STATUS_NOT_MATCHING, "payment.method.status.not.support.current");
            }

            payment.setActivated(true);
            payment.setPaymentMethod(paymentMethod);
            payment.setAmount(orderResponse.getFinalPrice()); 
            payment.setOrderId(orderResponse.getId());

            String action = isUpdate ? "UPDATE" : "INIT";
            String transactionId = String.join("_", paymentMethod.getCode().toUpperCase(), action, UUID.randomUUID().toString());

            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .eventId(UUID.randomUUID())
                .transactionId(transactionId)
                .status(PaymentEnum.PENDING)
                .payment(payment) 
                .note(isUpdate ? "User changed payment method" : "Initial payment request")
                .build();

            payment.getPaymentTransactions().add(paymentTransaction);

            this.paymentRepository.save(payment);

            PaymentResponse paymentResponse = this.paymentMapper.toDto(payment);
            if (!paymentMethod.getCode().equalsIgnoreCase(this.paymentCodProperties.getPartnerCode())) {
                PaymentStrategy paymentStrategy = this.paymentProcessorFactory.getProcessor(paymentMethod.getCode().toLowerCase());
                paymentResponse = paymentStrategy.process(payment);
            }

            return paymentResponse;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [upSertPayment] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkExistedPayment(UUID orderId, UUID excludedId){
        try {
            Optional<Payment> optional;
            if(excludedId == null){
                optional = this.paymentRepository.findDuplicateForCreate(orderId);
            } else {
                optional = this.paymentRepository.findDuplicateForUpdate(orderId, excludedId);
            }
            optional.ifPresent(p -> {
                throw new ServiceException(
                    EnumError.PAYMENT_PAYMENT_ERR_NOT_FOUND_ORDER_ID, 
                    "payment.not.found.orderId",
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
}
