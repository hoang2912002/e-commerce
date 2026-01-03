package com.fashion.api_gateway.common.enums;
import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum EnumError {
    API_GATEWAY_CALL_API("API-GATEWAY-CALL-API","Call API error",HttpStatus.BAD_REQUEST),
    API_GATEWAY_INTERNAL_ERROR("API-GATEWAY-INTERNAL-ERROR","Internal server error",HttpStatus.INTERNAL_SERVER_ERROR),
    API_GATEWAY_UNAUTHORIZED("API-GATEWAY-UNAUTHORIZED","Unauthorized",HttpStatus.UNAUTHORIZED)
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
