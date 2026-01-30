package com.fashion.payment.service.impls;

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
import com.fashion.payment.common.enums.PaymentMethodEnum;
import com.fashion.payment.common.util.PageableUtils;
import com.fashion.payment.common.util.SpecificationUtils;
import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.search.SearchModel;
import com.fashion.payment.dto.request.search.SearchOption;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.entity.PaymentMethod;
import com.fashion.payment.exception.ServiceException;
import com.fashion.payment.mapper.PaymentMethodMapper;
import com.fashion.payment.repository.PaymentMethodRepository;
import com.fashion.payment.service.PaymentMethodService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentMethodServiceImpl implements PaymentMethodService{
    PaymentMethodRepository paymentMethodRepository;
    PaymentMethodMapper paymentMethodMapper;
    
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request) {
        log.info("PAYMENT-SERVICE: [createPaymentMethod] Start create payment method");
        try {
            this.checkExistedPaymentMethod(request.getCode(),request.getName(),request.getId());
            PaymentMethod paymentMethod = this.paymentMethodMapper.toValidated(request);
            paymentMethod.setCode(request.getCode().toLowerCase());
            paymentMethod.setStatus(PaymentMethodEnum.ACTIVE);
            paymentMethod.setActivated(true);
            return paymentMethodMapper.toDto(this.paymentMethodRepository.save(paymentMethod));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [createPaymentMethod] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public PaymentMethodResponse updatePaymentMethod(PaymentMethodRequest request) {
        log.info("PAYMENT-SERVICE: [updatePaymentMethod] Start update payment method");
        try {
            this.checkExistedPaymentMethod(request.getCode(),request.getName(),request.getId());
            PaymentMethod paymentMethod = this.paymentMethodRepository.lockPaymentMethodById(request.getId()).orElseThrow(
                () -> new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_ERR_NOT_FOUND_ID,"payment.method.not.found.id", Map.of("id",request.getId()))
            );
            this.paymentMethodMapper.toUpdate(paymentMethod, request);
            paymentMethod.setActivated(true);
            return paymentMethodMapper.toDto(this.paymentMethodRepository.saveAndFlush(paymentMethod));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [updatePaymentMethod] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethodResponse getPaymentMethodById(Long id) {
        try {
            PaymentMethod paymentMethod = this.paymentMethodRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_ERR_NOT_FOUND_ID,"payment.method.not.found.id", Map.of("id",id))
            );
            return paymentMethodMapper.toDto(paymentMethod);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [getPaymentMethodById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<PaymentMethodResponse>> getAllPaymentMethod(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(PaymentMethod.class);

            Specification<PaymentMethod> spec = new SpecificationUtils<PaymentMethod>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<PaymentMethod> paymentMethods = this.paymentMethodRepository.findAll(spec, pageRequest);
            List<PaymentMethodResponse> paymentMethodResponses = this.paymentMethodMapper.toDto(paymentMethods.getContent());
            return PageableUtils.<PaymentMethod, PaymentMethodResponse>buildPaginationResponse(pageRequest, paymentMethods, paymentMethodResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [getAllPaymentMethod] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deletePaymentMethodById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deletePaymentMethodById'");
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentMethod getPaymentMethodByIdOrCode(Long id, String code) {
        try {
            boolean excludedCode = code != null;
            PaymentMethod paymentMethod = (excludedCode ? 
                this.paymentMethodRepository.findByCode(code.toLowerCase()) :
                this.paymentMethodRepository.findById(id)
            ).orElseThrow(
                () -> {
                    return excludedCode ? 
                        new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_DATA_EXISTED_CODE, "payment.not.found.code", Map.of("paymentMethodCode", code)) :
                        new ServiceException(EnumError.PAYMENT_PAYMENT_METHOD_ERR_NOT_FOUND_ID, "payment.not.found.id", Map.of("paymentMethodId", id))
                    ;
                }
            );
            return paymentMethod;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PAYMENT-SERVICE: [getPaymentMethodByIdOrCode] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    

    private void checkExistedPaymentMethod(String code, String name, Long excludedId){
        try {
            Optional<PaymentMethod> optional;
            if(excludedId == null){
                optional = this.paymentMethodRepository.findDuplicateForCreate(code, name);
            } else {
                optional = this.paymentMethodRepository.findDuplicateForUpdate(code, name, excludedId);
            }
            optional.ifPresent(p -> {
                if(p.getCode().equals(code)){
                    throw new ServiceException(
                        EnumError.PAYMENT_PAYMENT_METHOD_DATA_EXISTED_CODE, 
                        "payment.method.exist.code",
                        Map.of("code", code
                    ));
                } else {
                    throw new ServiceException(
                        EnumError.PAYMENT_PAYMENT_METHOD_DATA_EXISTED_NAME, 
                        "payment.method.name",
                        Map.of("name", name
                    ));
                }
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkExistedPaymentMethod] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
