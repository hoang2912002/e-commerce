package com.fashion.identity.dto.request.search.permission;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionSearchOption {
    Integer page = 0;
    Integer size = 10;
    String sort = "createDate,desc";

    private String normalize(Object value) {
        if (value == null) {
            return (value instanceof Integer) ? "0" : "";
        }
        if (value instanceof String) {
            return ((String) value).trim().toLowerCase();
        }
        return String.valueOf(value);
    }

    public String hashKey() {
        return String.join("-",
            normalize(page),
            normalize(size),
            normalize(sort)
        );
    }
}
