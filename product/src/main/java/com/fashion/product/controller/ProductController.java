package com.fashion.product.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fashion.product.common.annotation.ApiMessageResponse;
import com.fashion.product.common.annotation.InternalEndpoint;
import com.fashion.product.dto.request.ProductRequest;
import com.fashion.product.dto.request.ProductRequest.InnerInternalProductRequest;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.ProductResponse;
import com.fashion.product.entity.Product;
import com.fashion.product.mapper.ProductMapper;
import com.fashion.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    ProductMapper productMapper;

    @PostMapping("")
    @ApiMessageResponse("product.success.create")
    public ResponseEntity<ProductResponse> createProduct(
        @RequestBody @Validated(ProductRequest.Create.class) ProductRequest product
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.createProduct(product));
    }
    
    @PutMapping("")
    @ApiMessageResponse("product.success.update")
    public ResponseEntity<ProductResponse> updateProduct(
        @RequestBody @Validated(ProductRequest.Update.class) ProductRequest product
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.productService.updateProduct(product));
    }

    @GetMapping("/{id}")
    @ApiMessageResponse("product.success.get.single")
    public ResponseEntity<ProductResponse> getProductById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.productService.getProductById(id));
    }

    @GetMapping("")
    @ApiMessageResponse("product.success.get.all")
    public ResponseEntity<PaginationResponse<List<ProductResponse>>> getAllProduct(
        SearchRequest request
    ){
        return ResponseEntity.ok(this.productService.getAllProduct(request));
    }
    
    @DeleteMapping("/{id}")
    @ApiMessageResponse("product.success.delete")
    public ResponseEntity<Void> deleteProductById(
        @PathVariable("id") UUID id
    ){
        return ResponseEntity.ok(null);
    }

    //------------------Internal endpoint-----------------------
    @InternalEndpoint
    @GetMapping("/internal/validate-internal-product-product-sku")
    @ApiMessageResponse("product.success.internal.get.single")
    public ResponseEntity<Void> validateInternalProductById(
        @RequestParam UUID productId,
        @RequestParam UUID productSkuId
    ) {
        this.productService.validateInternalProductById(productId,productSkuId);
        return ResponseEntity.noContent().build();
    }
    
    @InternalEndpoint
    @GetMapping("/internal/get-internal-product-by-id")
    @ApiMessageResponse("product.success.internal.get.single")
    public ResponseEntity<ProductResponse> getInternalProductById(
        @RequestParam UUID productId
    ) {
        return ResponseEntity.ok(this.productService.getProductById(productId));
    }

    @InternalEndpoint
    @PostMapping("/internal/get-internal-product-and-product-sku-and-check-approval-by-id")
    @ApiMessageResponse("product.success.internal.get.single")
    public ResponseEntity<List<ProductResponse>> getInternalProductAndProductSkuById(
        @RequestBody InnerInternalProductRequest request
    ) {
        return ResponseEntity.ok(this.productService.getInternalProductByIdAndCheckApproval(request));
    }
}
