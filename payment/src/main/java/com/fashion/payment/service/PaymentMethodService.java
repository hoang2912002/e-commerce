package com.fashion.payment.service;

import java.util.List;

import com.fashion.payment.dto.request.PaymentMethodRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentMethodResponse;

public interface PaymentMethodService {
    PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request);
    PaymentMethodResponse updatePaymentMethod(PaymentMethodRequest request);
    PaymentMethodResponse getPaymentMethodById(Long id);
    PaginationResponse<List<PaymentMethodResponse>> getAllPaymentMethod(SearchRequest request);
    void deletePaymentMethodById(Long id);
}
