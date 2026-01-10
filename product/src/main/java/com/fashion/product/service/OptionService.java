package com.fashion.product.service;

import java.util.List;

import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.Option;

public interface OptionService {
    public OptionResponse createOption(Option option);
    public OptionResponse updateOption(Option option);
    public Option getRawOptionById(Long id);
    public OptionResponse getOptionById(Long id);
    public PaginationResponse<List<OptionResponse>> getAllOption(SearchRequest request);
}
