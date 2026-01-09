package com.fashion.product.common.util;

import java.util.Map;
import java.util.UUID;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.exception.ServiceException;

public class ConvertUuidUtil {
    public static UUID toUuid(Object id) {
        if (id == null) {
            throw new ServiceException(EnumError.NOTIFICATION_INVALID_FORMAT_UUID, "server.id.must.not.be.null");
        }

        // Nếu bản thân nó đã là UUID rồi thì trả về luôn (tránh parse thừa)
        if (id instanceof UUID) {
            return (UUID) id;
        }

        // Nếu là String thì mới tiến hành parse
        try {
            return UUID.fromString(id.toString());
        } catch (IllegalArgumentException e) {
            throw new ServiceException(
                EnumError.NOTIFICATION_INVALID_FORMAT_UUID, 
                "server.id.must.not.be.null", 
                Map.of("id", id.toString())
            );
        }
    }
}
