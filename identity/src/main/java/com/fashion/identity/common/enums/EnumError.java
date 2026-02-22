package com.fashion.identity.common.enums;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumError {
    // Address 
    IDENTITY_ADDRESS_INVALID_OWNER("IDENTITY-ADDRESS-INVALID-OWNER","The address belongs only to the store or the user:",HttpStatus.BAD_REQUEST),
    IDENTITY_ADDRESS_DATA_EXISTED("IDENTITY-ADDRESS-DATA-EXISTED", "The address already existed:",HttpStatus.BAD_REQUEST),
    IDENTITY_ADDRESS_ERR_NOT_FOUND_ID("IDENTITY-ADDRESS-ERR-NOT-FOUND-ID","Not found address with id:",HttpStatus.BAD_REQUEST),
    // Auth
    IDENTITY_USER_ERR_NOT_FOUND_USERNAME("IDENTITY-USER_ERR-NOT-FOUND-USERNAME","Not found user with userName:",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_ERR_NOT_FOUND_USERNAME_PASSWORD("IDENTITY-USER_ERR-NOT-FOUND-USERNAME-PASSWORD","Invalid account/password",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_INVALID_REFRESH_TOKEN("IDENTITY-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    IDENTITY_AUTHENTICATION_FAILED("IDENTITY-AUTHENTICATION-FAILED","Authentication failed for user:",HttpStatus.UNAUTHORIZED),
    IDENTITY_AUTHENTICATION_INVALID_VERIFY_CODE("IDENTITY-AUTHENTICATION-INVALID-VERIFY-CODE","The account has not been verified:",HttpStatus.BAD_REQUEST),
    // User
    IDENTITY_USER_DATA_EXISTED_EMAIL("IDENTITY-USER-DATA-EXISTED-EMAIL","User already exists with the given Email:", HttpStatus.BAD_REQUEST),
    IDENTITY_USER_DATA_EXISTED_PHONE_NUMBER("IDENTITY-USER-DATA-EXISTED-PHONE-NUMBER","User already exists with the given Phone number:", HttpStatus.BAD_REQUEST),
    IDENTITY_USER_ERR_NOT_FOUND_ID("IDENTITY-USER-ERR-NOT-FOUND-ID","Not found user with id:",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_ERR_NOT_FOUND_EMAIL("IDENTITY-USER-ERR-NOT-FOUND-EMAIL","Not found user with email:",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_INVALID_VERIFY_CODE("IDENTITY-USER-INVALID-VERIFY-CODE","Incorrect verification code:",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_INVALID_VERIFY_EXPIRATION("IDENTITY-USER-INVALID-VERIFY-EXPIRATION","The verification code has expired:",HttpStatus.BAD_REQUEST),
    // Role
    IDENTITY_ROLE_DATA_EXISTED_NAME("IDENTITY-ROLE-DATA-EXISTED-NAME","Role already exists with the given Name:",HttpStatus.CONFLICT),
    IDENTITY_ROLE_ERR_NOT_FOUND_ID("IDENTITY-ROLE-ERR-NOT-FOUND-ID","Not found role with id:",HttpStatus.BAD_REQUEST),
    //----------------Permission-------------------
    IDENTITY_PERMISSION_DATA_EXISTED_APIPATH("IDENTITY-PERMISSION-DTE-APIPATH","Permission already exists with the given Api path:",HttpStatus.CONFLICT),
    IDENTITY_PERMISSION_DATA_EXISTED_APIPATH_METHOD_SERVICE("IDENTITY-PERMISSION-DTE-APIPATH-METHOD-SERVICE","Permission already exists with the given Api path, Method, Service:",HttpStatus.CONFLICT),
    IDENTITY_PERMISSION_ERR_NOT_FOUND_ID("IDENTITY-PERMISSION-ERR-NOT-FOUND-ID","Not found permission with id:",HttpStatus.BAD_REQUEST),
    IDENTITY_PERMISSION_ACCESS_DENIED("IDENTITY-PERMISSION-ACCESS-DENIED","You do not have permission to access this resource.",HttpStatus.FORBIDDEN),

    // Kafka
    IDENTITY_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("IDENTITY-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),

    // Server
    IDENTITY_INTERNAL_ERROR_CALL_API("IDENTITY-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    IDENTITY_VALIDATION_ERROR("IDENTITY-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    IDENTITY_INVALID_FORMAT_UUID("IDENTITY-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    // Resilience4j
    IDENTITY_RESILIENCE4J_CIRCUIT_BREAKER_OPEN("IDENTITY-RESILIENCE4J-CIRCUIT-BREAKER-OPEN", "Service is currently unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE),
    IDENTITY_RESILIENCE4J_RATE_LIMITER("IDENTITY-RESILIENCE4J-RATE-LIMITER", "Too many requests. Please try again later.", HttpStatus.TOO_MANY_REQUESTS),
    IDENTITY_RESILIENCE4J_RETRY("IDENTITY-RESILIENCE4J-RETRY", "Service is currently unavailable. Retrying...", HttpStatus.SERVICE_UNAVAILABLE),
    ;
    
    String code;
    String defaultMessage;
    HttpStatus httpStatus;

    public static EnumError fromCode(String code) {
        for (EnumError e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown DispatchError code: " + code);
    }
}
