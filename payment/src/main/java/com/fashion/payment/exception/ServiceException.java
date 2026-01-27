package com.fashion.payment.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fashion.payment.common.enums.EnumError;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServiceException extends RuntimeException{
    EnumError enumError;
    String errorCode;
    String messageCode;
    HttpStatus httpStatus;
    Map<String, Object> errors;

    public ServiceException(EnumError error){
        this(error, error.name().toLowerCase(), null);
    }

    public ServiceException(EnumError error, String messageCode){
        this(error, messageCode, null);
    }
    
    public ServiceException(EnumError error, String messageCode, Map<String, Object> errors){
        super(error.getDefaultMessage());
        this.enumError = error;
        this.errorCode = error.getCode();
        this.messageCode = messageCode;
        this.httpStatus = error.getHttpStatus();
        this.errors = errors;
    }

    
}
