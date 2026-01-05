package com.fashion.resource.common.enums;

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
    RESOURCE_USER_ERR_NOT_FOUND_USERNAME("RESOURCE-USER_ERR-NOT-FOUND-USERNAME","Not found user with userName:",HttpStatus.BAD_REQUEST),
    RESOURCE_USER_INVALID_REFRESH_TOKEN("RESOURCE-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    RESOURCE_AUTHENTICATION_FAILED("RESOURCE-AUTHENTICATION-FAILED","Authentication failed for user:",HttpStatus.UNAUTHORIZED),
    
    // File
    RESOURCE_FILE_ERR_NOT_FOUND_NAME("RESOURCE-FILE-ERR-NOT-FOUND-NAME","Not found user with name:",HttpStatus.BAD_REQUEST),
    
    // Server
    RESOURCE_INTERNAL_ERROR_CALL_API("RESOURCE-INTERNAL-ERROR-CALL-API", "Call Resource Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_VALIDATION_ERROR("RESOURCE-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
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
