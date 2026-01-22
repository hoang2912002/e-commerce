package com.fashion.product.service;

import java.util.List;

import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionValueResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.OptionValue;

public interface OptionValueService {
    public OptionValueResponse createOptionValue(OptionValue optionValue);
    public OptionValueResponse updateOptionValue(OptionValue optionValue);
    public OptionValueResponse getOptionValueById(Long id);
    public PaginationResponse<List<OptionValueResponse>> getAllOptionValue(SearchRequest request);
}
