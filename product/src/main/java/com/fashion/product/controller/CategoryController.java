package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.CategoryRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.CategoryResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.mapper.CategoryMapper;
import com.fashion.product.service.CategoryService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Retry(name = "order-service", fallbackMethod = "resilience4jRetryFallback")
@CircuitBreaker(name = "order-service", fallbackMethod = "resilience4jCircuitBreakerFallback")
@RateLimiter(name = "order-service", fallbackMethod = "resilience4jRateLimiterFallback")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController extends R4jFallback {
    CategoryService categoryService;
    CategoryMapper categoryMapper;

    @PostMapping("")
    @ApiMessageResponse("category.success.create")
    public ResponseEntity<CategoryResponse> createCategory(
        @RequestBody @Validated(CategoryRequest.Create.class) CategoryRequest category
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.categoryService.createCategory(this.categoryMapper.toValidated(category)));
    }

    @PutMapping("")
    @ApiMessageResponse("category.success.update")
    public ResponseEntity<CategoryResponse> updateCategory(
        @RequestBody @Validated(CategoryRequest.Create.class) CategoryRequest category
    ) {        
        return ResponseEntity.ok(this.categoryService.updateCategory(this.categoryMapper.toValidated(category)));
    }
    
    @GetMapping("/{id}")
    @ApiMessageResponse("category.success.get.single")
    public ResponseEntity<CategoryResponse> getCategoryById(
        @PathVariable("id") UUID id,
        @RequestParam("version") Long version
    ) {
        return ResponseEntity.ok(this.categoryService.getCategoryById(id, version));
    }

    @GetMapping("")
    @ApiMessageResponse("category.success.get.all")
    public ResponseEntity<PaginationResponse<List<CategoryResponse>>> getAllCategory(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.categoryService.getAllCategory(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("category.success.delete")
    public ResponseEntity<Void> deleteCategoryById(
        @PathVariable("id") UUID id
    ){
        return ResponseEntity.ok(null);
    }
}
