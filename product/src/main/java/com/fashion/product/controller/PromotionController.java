package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.dto.request.PromotionRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.entity.Promotion;
import com.fashion.product.mapper.PromotionMapper;
import com.fashion.product.service.PromotionService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {
    PromotionService promotionService;
    PromotionMapper promotionMapper;

    @PostMapping("")
    @ApiMessageResponse("promotion.success.create")
    public ResponseEntity<PromotionResponse> createPromotion(
        @RequestBody @Validated(PromotionRequest.Create.class) PromotionRequest promotion
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            this.promotionService.createPromotion(
                promotion
            )
        );
    }
    
    @PutMapping("")
    @ApiMessageResponse("promotion.success.update")
    public ResponseEntity<PromotionResponse> updatePromotionById(
        @RequestBody @Validated(PromotionRequest.Update.class) PromotionRequest promotion
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.promotionService.updatePromotion(
            promotion
        ));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("promotion.success.get.single")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable("id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.promotionService.getPromotionById(id));
    }
    
    @GetMapping("")
    @ApiMessageResponse("promotion.success.get.all")
    public ResponseEntity<PaginationResponse<List<PromotionResponse>>> getAllPromotion(
        @ModelAttribute SearchRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.promotionService.getAllPromotion(request));
    }
}
