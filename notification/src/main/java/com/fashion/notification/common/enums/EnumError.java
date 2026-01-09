package com.fashion.notification.common.enums;

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
    NOTIFICATION_USER_INVALID_REFRESH_TOKEN("NOTIFICATION-USER-INVALID-REFRESH-TOKEN","User already with invalid Refresh token:",HttpStatus.BAD_REQUEST),
    

    // Server
    NOTIFICATION_INTERNAL_ERROR_CALL_API("NOTIFICATION-INTERNAL-ERROR-CALL-API", "Call Identity Service api error", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_VALIDATION_ERROR("NOTIFICATION-VALIDATION-ERROR","Validation error",HttpStatus.BAD_REQUEST),
    NOTIFICATION_INVALID_FORMAT_UUID("NOTIFICATION-INVALID-FORMAT-UUID", "Wrong ID data type format", HttpStatus.BAD_REQUEST),

    // Kafka
    NOTIFICATION_KAFKA_REQUEST_TIME_OUT_WITH_BROKER("NOTIFICATION-KAFKA-REQUEST-TIME-OUT-WITH-BROKER","Can not connect to Kafka", HttpStatus.REQUEST_TIMEOUT),
    NOTIFICATION_KAFKA_DATA_SERIALIZATION_ERROR("NOTIFICATION-KAFKA-DATA-SERIALIZATION-ERROR","Can not connect to Kafka", HttpStatus.BAD_REQUEST),
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
