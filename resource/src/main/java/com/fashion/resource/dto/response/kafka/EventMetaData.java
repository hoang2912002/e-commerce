package com.fashion.resource.dto.response.kafka;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventMetaData {
   UUID eventId;        // duy nhất toàn hệ thống 
   String eventType;    // PERMISSION_REGISTER, ORDER_PAID...
   String source;       // resource-service
   int version;         // version của event schema
}
