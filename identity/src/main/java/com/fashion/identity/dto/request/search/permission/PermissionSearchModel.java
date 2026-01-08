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
public class PermissionSearchModel {
    String q;
    Boolean activated;

    String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public String hashKey() {
        return String.join("-",
            normalize(q),
            String.valueOf(activated)
        );
    }
}
