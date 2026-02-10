package com.fashion.product.service;

import java.util.List;
import java.util.UUID;

import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.entity.Category;

public interface CategoryService {
    CategoryResponse createCategory(Category category);
    CategoryResponse updateCategory(Category category);
    CategoryResponse getCategoryById(UUID id, Long version);
    PaginationResponse<List<CategoryResponse>> getAllCategory(SearchRequest request);
    Category findRawCategoryById(UUID id);
    List<Category> getCategoryTreeStartByListId(List<UUID> ids);
    boolean isLeaf(Category category);
    Category fetchAndValidateCategory(UUID id);
}
