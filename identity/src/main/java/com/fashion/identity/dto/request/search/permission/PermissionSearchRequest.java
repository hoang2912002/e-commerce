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
public class PermissionSearchRequest {
    PermissionSearchModel searchModel = new PermissionSearchModel();
    PermissionSearchOption searchOption = new PermissionSearchOption();

    public String hashKey() {
        return "option:" + searchModel.hashKey() + "|filter:" + searchOption.hashKey();
    }
}
