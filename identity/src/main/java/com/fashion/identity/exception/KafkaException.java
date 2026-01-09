package com.fashion.identity.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fashion.identity.common.enums.EnumError;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaException extends RuntimeException{
    EnumError enumError;
    String errorCode;
    String messageCode;
    HttpStatus httpStatus;
    Map<String, Object> errors;

    public KafkaException(EnumError error){
        this(error, error.name().toLowerCase(), null);
    }

    public KafkaException(EnumError error, String messageCode){
        this(error, messageCode, null);
    }
    
    public KafkaException(EnumError error, String messageCode, Map<String, Object> errors){
        super(error.getDefaultMessage());
        this.enumError = error;
        this.errorCode = error.getCode();
        this.messageCode = messageCode;
        this.httpStatus = error.getHttpStatus();
        this.errors = errors;
    }
}
