package com.fashion.order.controller;

import java.util.List;
import java.util.UUID;

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

import com.fashion.order.common.annotation.ApiMessageResponse;
import com.fashion.order.common.annotation.InternalEndpoint;
import com.fashion.order.dto.request.OrderRequest;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.OrderResponse;
import com.fashion.order.dto.response.PaginationResponse;
import com.fashion.order.service.OrderService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping("")
    @ApiMessageResponse("order.success.create")
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody @Validated(OrderRequest.Create.class) OrderRequest orderRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.orderService.createOrder(orderRequest));
    }
    
    @PutMapping("")
    @ApiMessageResponse("order.success.update")
    public ResponseEntity<OrderResponse> updateOrder(
        @RequestBody @Validated(OrderRequest.Update.class) OrderRequest orderRequest
    ) {
        return ResponseEntity.ok(
            this.orderService.updateOrder(
                orderRequest
            )
        );
    }

    @PutMapping("/approvalStatus/{id}")
    @ApiMessageResponse("order.success.update.status")
    public ResponseEntity<OrderResponse> updateApprovalStatus(
        @PathVariable("id") UUID id,
        @RequestBody() OrderRequest order
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.orderService.updateOrderStatus(id, order.getStatus(), order.getNote(), order.getVersion()));
    }
    

    @GetMapping("/{id}")
    @ApiMessageResponse("order.success.get.single")
    public ResponseEntity<OrderResponse> getOrderById(
        @PathVariable("id") UUID id
    ) {
        return ResponseEntity.ok(this.orderService.getOrderById(id));
    }
    
    @GetMapping("/history")
    @ApiMessageResponse("order.success.get.single.code")
    public ResponseEntity<OrderResponse> getOrderByCode(
        @RequestParam("code") String code
    ) {
        return ResponseEntity.ok(this.orderService.getOrderByCode(code));
    }

    @GetMapping("")
    @ApiMessageResponse("order.success.get.all")
    public ResponseEntity<PaginationResponse<List<OrderResponse>>> getAllOrder(
        @ModelAttribute SearchRequest request
    ){
        return ResponseEntity.ok(this.orderService.getAllOrder(request));
    }
    

    @DeleteMapping("/{id}")
    @ApiMessageResponse("order.success.delete")
    public void deleteOrderById(
        @PathVariable("id") Long id
    ) {
        ResponseEntity.noContent().build();
    }

    //-------------Internal endpoint-------------------
    @GetMapping("/internal/get-internal-order-and-check-approval-by-id")
    @InternalEndpoint
    @ApiMessageResponse("order.success.get.single")
    public ResponseEntity<OrderResponse> getInternalOrderAndCheckStatusById(@RequestParam UUID orderId) {
        return ResponseEntity.ok(this.orderService.getInternalOrderById(orderId, true));
    }
    
}
