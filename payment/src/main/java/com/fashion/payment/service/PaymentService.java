package com.fashion.payment.service;

import java.util.List;
import java.util.UUID;

import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse updatePayment(PaymentRequest request);
    PaymentResponse getPaymentById(UUID id, String date, Long version);
    PaginationResponse<List<PaymentResponse>> getAllPayment(SearchRequest request);
    void upSertPayment(InnerInternalPayment request, UUID eventId);
    void deletePaymentById(UUID id);
}
