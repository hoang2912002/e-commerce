package com.fashion.product.dto.request.search;

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
public class SearchRequest {
    SearchModel searchModel = new SearchModel();
    SearchOption searchOption = new SearchOption();
    
    public String hashKey() {
        return "option:" + searchModel.hashKey() + "|filter:" + searchOption.hashKey();
    }

}
