package com.fashion.payment.integration.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

import com.fashion.payment.common.enums.EnumError;
import com.fashion.payment.common.response.ApiResponse;
import com.fashion.payment.exception.ServiceException;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class FeignClientConfigError implements ErrorDecoder{
    ObjectMapper mapper;
    @Override
    //Centralized Error Handling with Feign ErrorDecoder (Xử lý lỗi tập trung với ErrorDecoder).
    public Exception decode(String methodKey, Response response) {
        // try-with-resources để tự động đóng InputStream
        try (InputStream body = response.body().asInputStream()) {
            ApiResponse<?> errorRes = mapper.readValue(body, ApiResponse.class);
            log.error("PRODUCT-SERVICE call Feign failed: {} - {}", methodKey, errorRes.getErrorCode());
            return new ServiceException(
                EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, 
                errorRes.getErrorCode() != null ? errorRes.getErrorCode() : "server.error.internal",
                errorRes.getErrors()
            );
        } catch (IOException e) {
            return new ServiceException(EnumError.PAYMENT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
