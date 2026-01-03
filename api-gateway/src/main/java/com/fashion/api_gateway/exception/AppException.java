package com.fashion.api_gateway.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fashion.api_gateway.common.enums.EnumError;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppException extends RuntimeException {
    EnumError enumError;
    String errorCode;
    String messageCode;
    HttpStatus httpStatus;
    Map<String, Object> errors;
    
    public AppException(EnumError error){
        this(error, error.name().toLowerCase(), null);
    }

    public AppException(EnumError error, String messageCode){
        this(error, messageCode, null);
    }

    public AppException(EnumError error, String messageCode, Map<String, Object> errors){
        super(error.getDefaultMessage());
        this.enumError = error;
        this.errorCode = error.getCode();
        this.messageCode = messageCode;
        this.httpStatus = error.getHttpStatus();
        this.errors = errors;
    }
}
