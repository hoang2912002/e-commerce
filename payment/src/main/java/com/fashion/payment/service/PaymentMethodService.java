package com.fashion.payment.service;

import java.util.List;
import java.util.UUID;

import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;
import com.fashion.payment.entity.PaymentMethod;

public interface PaymentMethodService {
    PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request);
    PaymentMethodResponse updatePaymentMethod(PaymentMethodRequest request);
    PaymentMethodResponse getPaymentMethodById(Long id);
    PaginationResponse<List<PaymentMethodResponse>> getAllPaymentMethod(SearchRequest request);
    PaymentMethod getPaymentMethodByIdOrCode(Long id, String code);
    void deletePaymentMethodById(Long id);
}
