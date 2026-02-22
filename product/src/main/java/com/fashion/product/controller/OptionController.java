package com.fashion.product.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.OptionRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.OptionResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.mapper.OptionMapper;
import com.fashion.product.service.OptionService;

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
@RequestMapping("/options")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OptionController extends R4jFallback {
    OptionService optionService;
    OptionMapper optionMapper;

    @PostMapping("")
    @ApiMessageResponse("option.success.create")
    public ResponseEntity<OptionResponse> createOption(
        @RequestBody @Validated(OptionRequest.Create.class) OptionRequest option
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.optionService.createOption(optionMapper.toValidated(option)));
    }

    @PutMapping("")
    @ApiMessageResponse("option.success.update")
    public ResponseEntity<OptionResponse> updateOption(
        @RequestBody @Validated(OptionRequest.Update.class) OptionRequest option
    ) {        
        return ResponseEntity.ok(this.optionService.updateOption(optionMapper.toValidated(option)));
    }
    
    @GetMapping("/{id}")
    @ApiMessageResponse("option.success.get.single")
    public ResponseEntity<OptionResponse> getOptionById(
        @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(this.optionService.getOptionById(id));
    }
    
    @GetMapping("")
    @ApiMessageResponse("option.success.get.all")
    public ResponseEntity<PaginationResponse<List<OptionResponse>>> getAllOption(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.ok(this.optionService.getAllOption(request));
    }

    @DeleteMapping("/{id}")
    @ApiMessageResponse("option.success.delete")
    public ResponseEntity<Void> deleteOptionById(@PathVariable("id") Long id)
    {
        return ResponseEntity.ok(null);
    }
}
