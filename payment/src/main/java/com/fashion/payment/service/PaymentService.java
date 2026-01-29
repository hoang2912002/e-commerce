package com.fashion.payment.service;

import java.util.List;
import java.util.UUID;

import com.fashion.payment.dto.request.PaymentRequest;
import com.fashion.payment.dto.request.search.SearchRequest;
import com.fashion.payment.dto.response.PaginationResponse;
import com.fashion.payment.dto.response.PaymentResponse;
import com.fashion.payment.dto.response.PaymentResponse.InnerInternalPayment;

public interface PaymentService {
    void UpSertPayment(InnerInternalPayment payment, UUID eventId);
    PaymentResponse createPayment(PaymentRequest request);
    PaymentResponse updatePayment(PaymentRequest request);
    PaymentResponse getPaymentById(UUID id);
    PaginationResponse<List<PaymentResponse>> getAllPayment(SearchRequest request);
    void deletePaymentById(UUID id);
}
