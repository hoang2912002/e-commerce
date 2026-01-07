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

    // Auth
    IDENTITY_USER_ERR_NOT_FOUND_USERNAME("IDENTITY-USER_ERR-NOT-FOUND-USERNAME","Not found user with userName:",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_ERR_NOT_FOUND_USERNAME_PASSWORD("IDENTITY-USER_ERR-NOT-FOUND-USERNAME-PASSWORD","Invalid account/password",HttpStatus.BAD_REQUEST),
    IDENTITY_USER_INVALID_REFRESH_TOKEN("IDENTITY-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    IDENTITY_AUTHENTICATION_FAILED("IDENTITY-AUTHENTICATION-FAILED","Authentication failed for user:",HttpStatus.UNAUTHORIZED),
    
    // User
    IDENTITY_USER_DATA_EXISTED_EMAIL("IDENTITY-USER-DATA-EXISTED-EMAIL","User already exists with the given Email:", HttpStatus.BAD_REQUEST),
    IDENTITY_USER_DATA_EXISTED_PHONE_NUMBER("IDENTITY-USER-DATA-EXISTED-PHONE-NUMBER","User already exists with the given Phone number:", HttpStatus.BAD_REQUEST),
    IDENTITY_USER_ERR_NOT_FOUND_ID("IDENTITY-USER-ERR-NOT-FOUND-ID","Not found user with id:",HttpStatus.BAD_REQUEST),
    // Role
    IDENTITY_ROLE_DATA_EXISTED_NAME("IDENTITY-ROLE-DATA-EXISTED-NAME","Role already exists with the given Name:",HttpStatus.CONFLICT),
    IDENTITY_ROLE_ERR_NOT_FOUND_ID("IDENTITY-ROLE-ERR-NOT-FOUND-ID","Not found role with id:",HttpStatus.BAD_REQUEST),
    

    // Server
    IDENTITY_INTERNAL_ERROR_CALL_API("IDENTITY-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    IDENTITY_VALIDATION_ERROR("IDENTITY-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    IDENTITY_INVALID_FORMAT_UUID("IDENTITY-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),
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
