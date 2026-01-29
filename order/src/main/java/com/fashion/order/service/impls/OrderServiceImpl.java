package com.fashion.order.service.impls;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.atn.SemanticContext.OR;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.order.common.enums.CouponEnum;
import com.fashion.order.common.enums.EnumError;
import com.fashion.order.common.enums.OrderEnum;
import com.fashion.order.common.enums.PaymentEnum;
import com.fashion.order.common.enums.ShippingEnum;
import com.fashion.order.common.response.ApiResponse;
import com.fashion.order.common.util.AsyncUtils;
import com.fashion.order.dto.request.OrderRequest;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetailRequest;
import com.fashion.order.dto.request.OrderDetailRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.order.dto.request.internal.AddressRequest;
import com.fashion.order.dto.request.internal.ProductRequest.InnerInternalProductRequest;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.OrderResponse;
import com.fashion.order.dto.response.PaginationResponse;
import com.fashion.order.dto.response.internal.AddressResponse;
import com.fashion.order.dto.response.internal.PaymentResponse;
import com.fashion.order.dto.response.internal.PaymentResponse.InnerInternalPayment;
import com.fashion.order.dto.response.internal.ProductResponse;
import com.fashion.order.dto.response.internal.ProductSkuResponse;
import com.fashion.order.dto.response.internal.ShippingResponse;
import com.fashion.order.dto.response.internal.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent.InternalOrderCreatedEvent;
import com.fashion.order.dto.response.internal.UserResponse;
import com.fashion.order.dto.response.internal.AddressResponse.InnerAddressResponse;
import com.fashion.order.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;
import com.fashion.order.dto.response.internal.ProductResponse.InnerProductResponse;
import com.fashion.order.entity.Coupon;
import com.fashion.order.entity.Order;
import com.fashion.order.entity.OrderDetail;
import com.fashion.order.exception.ServiceException;
import com.fashion.order.intergration.IdentityClient;
import com.fashion.order.intergration.InventoryClient;
import com.fashion.order.intergration.ProductClient;
import com.fashion.order.mapper.OrderMapper;
import com.fashion.order.repository.OrderRepository;
import com.fashion.order.service.CouponService;
import com.fashion.order.service.OrderService;
import com.fashion.order.service.provider.OrderUpdateStatusErrorProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class OrderServiceImpl implements OrderService{
    OrderMapper orderMapper;
    OrderRepository orderRepository;
    IdentityClient identityClient;
    ProductClient productClient;
    InventoryClient inventoryClient;
    CouponService couponService;
    OrderUpdateStatusErrorProvider orderUpdateStatusErrorProvider;
    ApplicationEventPublisher applicationEventPublisher;

    public static final Set<String> IMPORTANT_FIELDS_STATUS_CONFIRMED = Set.of(
        "receiverAddress",
        "receiverProvince",
        "receiverDistrict",
        "receiverWard",
        "paymentMethod",
        "receiverName",
        "receiverEmail",
        "receiverPhone"
    );
    
    public static final Set<String> IMPORTANT_FIELDS_STATUS_PENDING = Set.of(
        "receiverAddress",
        "receiverProvince",
        "receiverDistrict",
        "receiverWard",
        "receiverName",
        "receiverEmail",
        "receiverPhone",
        "paymentMethod",
        "totalItem",
        "totalPrice",
        "discountPrice",
        "finalPrice",
        "shippingFee",
        "userId",
        "addressId"
    );

    public static final Set<String> NOT_ALLOWED_FIELDS_STATUS_CONFIRMED = Set.of(
        "totalItem",
        "totalPrice",
        "discountPrice",
        "finalPrice",
        "status"
    );
     

    private static final Set<String> IGNORE_FIELDS_STATUS_CONFIRMED = Set.of(
        "id",
        "note",
        "orderDetails",
        "payment",
        "user",
        "coupon",
        "code",
        "shipping",
        "totalItem",
        "totalPrice",
        "discountPrice",
        "finalPrice",
        "status",
        "activated"
    );
    private static final Set<String> IGNORE_FIELDS_STATUS_PENDING = Set.of(
        "id",
        "paymentStatus",
        "shippingStatus",
        "status",
        // "shippingFee",
        "shippingId",
        "paymentId",
        "note",
        "orderDetails",
        "coupon",
        "code",
        "activated",
        "version"
        // "totalItem",
        // "totalPrice",
        // "discountPrice",
        // "finalPrice"
    );

    public static final Set<String> NOT_ALLOWED_FIELDS_STATUS_PENDING = Set.of(
        "id",
        "status"
    );
    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OrderResponse createOrder(OrderRequest request) {
        log.info("ORDER-SERVICE: [createOrder] start create order ....");
        try {
            Order order = this.orderMapper.toValidated(request);
            // Initialize kafka data
            OrderCreatedEvent kafkaEventData = OrderCreatedEvent.builder()
                .inventories(new HashSet<>())
                .payment(new InnerInternalPayment())
                .promotions(new HashMap<>())
                .shipping(new ShippingResponse())
                .build();
            Map<UUID, InnerOrderDetail_FromOrderRequest> odRequest = request.getOrderDetails().stream()
                .map(o -> {
                    if (o.getId() == null) {
                        o.setId(0L); 
                    }
                    return o;
                })
                .collect(Collectors.toMap(
                    o -> o.getProductSku().getId(), // Key: SkuId
                    o -> o,                         // Value: Chính đối tượng đó
                    (existing, replacement) -> existing // Xử lý nếu trùng SKU: giữ cái cũ (hoặc cộng dồn quantity tùy bạn)
                ));
            List<UUID> productIdList = odRequest.values().stream().map(o -> o.getProduct().getId()).distinct().toList();
            List<UUID> productSkuIdList = odRequest.values().stream().map(o -> o.getProductSku().getId()).distinct().toList();

            CompletableFuture<UserResponse> userFuture = (order.getUserId() != null)
                ? AsyncUtils.fetchAsync(() -> this.identityClient.getInternalUserById(order.getUserId()))
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<List<ProductResponse>> productFuture = (!productIdList.isEmpty() && !productSkuIdList.isEmpty())
                ? AsyncUtils.fetchAsync(() -> this.productClient.getInternalProductAndProductSkuById(
                    InnerInternalProductRequest.builder().productIdList(productIdList).productSkuIdList(productSkuIdList).build()
                ))
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> inventoryFuture = (!odRequest.isEmpty()) 
                ? AsyncUtils.fetchAsync(() -> this.inventoryClient.checkQuantityAvailableInventoryByProductSkuId(odRequest.values()))
                : CompletableFuture.completedFuture(null);

            try {
                CompletableFuture.allOf(userFuture, productFuture, inventoryFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }    
            UserResponse userResponse = userFuture.join();
            List<ProductResponse> productResponse = productFuture.join();
            final InnerAddressResponse address = !userResponse.getAddresses().isEmpty() ? userResponse.getAddresses().getLast() : new InnerAddressResponse();

            final Coupon coupon = this.couponService.validateCouponOrder(order.getCoupon().getId());

            List<OrderDetail> orderDetails = new ArrayList<>();
            BigDecimal totalItemPrice_Order = BigDecimal.ZERO;
            BigDecimal totalDiscountByPromotion = BigDecimal.ZERO;
            Integer totalItem_Order = 0;

            for (ProductResponse product : productResponse) {
                for (InnerProductSkuResponse pSku : product.getProductSkus()) {
                    if (!odRequest.containsKey(pSku.getId())) continue;

                    // Integer quantity = odRequest.get(pSku.getId()) != null ? odRequest.get(pSku.getId()).getQuantity() : 0;
                    Integer quantity = odRequest.get(pSku.getId()).getQuantity();
                    if (quantity == null || quantity <= 0) continue;
                    BigDecimal qty = BigDecimal.valueOf(quantity);

                    BigDecimal originalPricePerUnit = pSku.getPrice();
                    BigDecimal discountPerUnit = pSku.getPromotion() != null ? pSku.getPromotion().getDiscountFinal() : BigDecimal.ZERO;
                    BigDecimal finalPricePerUnit = originalPricePerUnit.subtract(discountPerUnit).max(BigDecimal.ZERO);

                    BigDecimal totalPrice_Detail = calculateTotalPrice(originalPricePerUnit,discountPerUnit,qty);

                    orderDetails.add(
                        OrderDetail.builder()
                            .activated(true)
                            .promotionDiscount(discountPerUnit)
                            .priceOriginal(originalPricePerUnit)
                            .price(finalPricePerUnit)
                            .quantity(quantity)
                            .totalPrice(totalPrice_Detail)
                            .productId(product.getId())
                            .productSkuId(pSku.getId())
                            .order(order)
                            .build()
                    );
                    if (pSku.getPromotion() != null) {
                        // ~quantity + 1 concurrency -quantity 
                        kafkaEventData.getPromotions().merge(pSku.getId(), ~quantity + 1, Integer::sum);
                    }
                    kafkaEventData.getInventories().add(
                        ReturnAvailableQuantity.builder()
                        .productId(product.getId())
                        .productSkuId(pSku.getId())
                        .circulationCount(quantity)
                        .isNegative(true) // true -> Decrease inventory quantity available (Negative) and opposite
                        .build()
                    );

                    // Summary total item/price for Order
                    totalItemPrice_Order = totalItemPrice_Order.add(originalPricePerUnit.multiply(qty));
                    totalDiscountByPromotion = totalDiscountByPromotion.add(discountPerUnit.multiply(qty));
                    totalItem_Order += quantity;
                }
            }
            // --- CREATE ORDER ---
            BigDecimal couponAmount = 
                coupon.getType().equals(CouponEnum.PERCENT) ? 
                totalItemPrice_Order.multiply(coupon.getCouponAmount()).divide(BigDecimal.valueOf(100),0, RoundingMode.HALF_UP) : 
                coupon.getCouponAmount();
            couponAmount = couponAmount.min(totalItemPrice_Order);
            BigDecimal totalDiscount = totalDiscountByPromotion.add(couponAmount);
            BigDecimal finalPrice = totalItemPrice_Order.subtract(totalDiscount);
            order.setActivated(true);
            order.setCoupon(coupon);
            order.setDiscountPrice(totalDiscount);
            order.setReceiverAddress(address.getAddress());
            order.setReceiverDistrict(address.getDistrict());
            order.setReceiverProvince(address.getProvince());
            order.setReceiverWard(address.getWard());
            order.setReceiverName(userResponse.getFullName());
            order.setReceiverEmail(userResponse.getEmail());
            order.setReceiverPhone(userResponse.getPhoneNumber());
            order.setStatus(OrderEnum.PENDING);
            order.setTotalItem(totalItem_Order);
            order.setTotalPrice(totalItemPrice_Order);
            order.setUserId(userResponse.getId());
            order.setAddressId(address.getId());
            order.setPaymentStatus(PaymentEnum.PENDING);
            order.setShippingStatus(ShippingEnum.WAITING);
            order.setFinalPrice(finalPrice);
            order.setShippingFee(null);
            order.setShippingId(null);
            order.setPaymentId(null);
            order.setCode(this.generateUniqueOrderCode());
            order.setOrderDetails(orderDetails);

            // DECREASE COUPON
            this.couponService.decreaseStock(coupon.getId());

            kafkaEventData.setPayment(
                InnerInternalPayment.builder()
                .status(order.getPaymentStatus())
                .amount(order.getFinalPrice())
                .paymentMethod(order.getPaymentMethod())
                .orderId(order.getId())
                .build()
            );
            // KAFKA SEND EVENT DECREASE: INVENTORY, PROMOTION. AND CREATE PAYMENT TRANSACTION.
            applicationEventPublisher.publishEvent(new InternalOrderCreatedEvent(this, kafkaEventData));
            return this.orderMapper.toDto(this.orderRepository.save(order));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [createOrder] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public OrderResponse updateOrder(OrderRequest request) {
        log.info("ORDER-SERVICE: [updateOrder] start update order ....");
        try {
            Order saveOrder = new Order();
            this.orderMapper.toUpdate(saveOrder,request);
            List<InnerOrderDetail_FromOrderRequest> odRequest = request.getOrderDetails();

            Order order = this.orderRepository.findById(request.getId()).orElseThrow(
                () -> new ServiceException(EnumError.ORDER_ORDER_ERR_NOT_FOUND_ID,"order.not.found.id", Map.of("id", saveOrder.getId()))
            );
            if(!order.getVersion().equals(saveOrder.getVersion())){
                throw new ServiceException(EnumError.ORDER_ORDER_INVALID_SIMILAR_VERSION, "order.version.notSimilar.currentVersion");
            }
            ApiResponse<UserResponse> response = this.identityClient.getInternalUserById(saveOrder.getUserId());
            UserResponse userResponse = response.getData();
            InnerAddressResponse address = !userResponse.getAddresses().isEmpty() ? 
                userResponse.getAddresses().stream().filter(a -> a.getId().equals(saveOrder.getAddressId())).findFirst().orElseGet(null) : null;
            if(address instanceof InnerAddressResponse){
                saveOrder.setReceiverAddress(address.getAddress());
                saveOrder.setReceiverDistrict(address.getDistrict());
                saveOrder.setReceiverProvince(address.getProvince());
                saveOrder.setReceiverWard(address.getWard());
            }
            saveOrder.setReceiverEmail(userResponse.getEmail());
            saveOrder.setReceiverName(userResponse.getFullName());
            saveOrder.setReceiverPhone(userResponse.getPhoneNumber());
            order.getStatus().validateUpdateOrder(orderUpdateStatusErrorProvider);
            OrderCreatedEvent eventData = null;
            if(order.getStatus().equals(OrderEnum.PENDING)){
                eventData = this.updateOrderStatusPending(order, saveOrder, odRequest);
            } else if (order.getStatus().equals(OrderEnum.CONFIRMED)) {
                // this.updateOrderStatusConfirmed(order, saveOrder, odRequest);
            }
            if(eventData != null){
                // Send kafka event
                applicationEventPublisher.publishEvent(new InternalOrderCreatedEvent(this, eventData));
            }
            return this.orderMapper.toDto(this.orderRepository.save(order));
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [updateOrder] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public OrderResponse getOrderByCode(String code) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrderByCode'");
    }

    @Override
    public OrderResponse updateOrderStatus(UUID id, OrderEnum status, String note) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateOrderStatus'");
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOrderById'");
    }

    @Override
    public void deleteOrderById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteOrderById'");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getInternalOrderById(UUID id, Boolean checkStatus){
        try {
            Order order = this.orderRepository.findById(id).orElseThrow(
                () -> new ServiceException(EnumError.ORDER_ORDER_ERR_NOT_FOUND_ID,"order.not.found.id", Map.of("id", id))
            );
            if(checkStatus){
                order.getStatus().validateUpdateOrder(orderUpdateStatusErrorProvider);
            }
            return this.orderMapper.toDto(order);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [getInternalOrderById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private String generateUniqueOrderCode() {
        final int MAX_ATTEMPTS = 5; 
        try {
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                String prefix = "HD" + LocalDate.now().getYear(); 
                String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); 
                String orderCode = prefix + "-" + randomPart;
                Order order = this.orderRepository.findByCode(orderCode).orElseGet(() -> null);
                if (order == null) {
                    return orderCode;
                }
            }
            throw new ServiceException(
                EnumError.ORDER_INTERNAL_ERROR_CALL_API, 
                "server.error.internal",
                Map.of("message", "Failed to generate unique order code after " + MAX_ATTEMPTS + " attempts.")
            );

        } catch (Exception e) {
            log.error("ORDER-SERVICE: [updateOrder] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<OrderResponse>> getAllOrder(SearchRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllOrder'");
    }

    private BigDecimal calculateTotalPrice(BigDecimal priceOriginal, BigDecimal discountAmount, BigDecimal quantity) {
        BigDecimal original = (priceOriginal == null) ? BigDecimal.ZERO : priceOriginal;
        BigDecimal discount = (discountAmount == null) ? BigDecimal.ZERO : discountAmount;

        // price = priceOriginal - discount
        BigDecimal priceAfterDiscount = original.subtract(discount);
        
        // Ensure final price not negative number
        BigDecimal finalUnitPrice = priceAfterDiscount.max(BigDecimal.ZERO);

        // totalPrice = price * quantity
        return finalUnitPrice.multiply(quantity);
    }
    

    private OrderCreatedEvent updateOrderStatusPending(Order orderDB, Order orderReq, List<InnerOrderDetail_FromOrderRequest> odRequest){
        try {
            Map<String, Object[]> changedFields = detectChangedFields(orderDB, orderReq, IGNORE_FIELDS_STATUS_PENDING);
            Coupon couponCurrentUp = this.couponService.validateCouponOrder(orderReq.getCoupon().getId());

            // KIỂM TRA THAY ĐỔI CHI PHÍ VẬN CHUYỂN (IMPORTANT CHECK) 
            boolean isShippingInfoChanged = changedFields.keySet().stream()
                .anyMatch(field -> 
                    field.startsWith("receiverAddress") || 
                    field.startsWith("receiverProvince") || 
                    field.startsWith("receiverDistrict") ||
                    field.startsWith("receiverWard") 
                );
            
            boolean isForbiddenChanged = changedFields.keySet()
                .stream()
                .anyMatch(NOT_ALLOWED_FIELDS_STATUS_PENDING::contains);
            
            if(isForbiddenChanged){
                String forbiddenField = changedFields.keySet().stream()
                    .filter(NOT_ALLOWED_FIELDS_STATUS_CONFIRMED::contains).findFirst().orElse("Unknown");
                throw new ServiceException(
                    EnumError.ORDER_ORDER_FIELD_FORBIDDEN_CANNOT_UPDATE_STATUS_CONFIRMED, 
                    "order.field.cannot.update.status.confirmed",
                    Map.of("field", forbiddenField)
                );
            }

            Map<UUID, OrderDetail> oldDetailsMap = orderDB.getOrderDetails().stream()
                .collect(Collectors.toMap(OrderDetail::getProductSkuId, Function.identity(), (a,b) -> a));

            List<OrderDetail> updatedOrderDetails = new ArrayList<>();

            // Initialize kafka data
            OrderCreatedEvent kafkaEventData = OrderCreatedEvent.builder()
                .inventories(new HashSet<>())
                .payment(new InnerInternalPayment())
                .promotions(new HashMap<>())
                .shipping(new ShippingResponse())
                .build();
            BigDecimal totalItemPrice_Order = BigDecimal.ZERO; // Tổng giá gốc (priceOriginal * quantity)
            BigDecimal totalDiscountByPromotion = BigDecimal.ZERO; // Tổng giảm giá từ Promotion (discountFinal * quantity)
            BigDecimal couponAmount = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            Integer totalItem_Order = 0;
            BigDecimal finalPrice = orderDB.getFinalPrice();

            Map<UUID, ReturnAvailableQuantity> inventoryUsageChangeMap = new HashMap<>();
            
            Set<Long> detailsToDelete = new HashSet();
            Map<UUID, OrderDetail> finalDetailsMap = new HashMap<>(oldDetailsMap);

            Map<UUID, InnerOrderDetail_FromOrderRequest> newDetailsMap = odRequest.stream()
                .collect(Collectors.toMap(o -> o.getProductSku().getId(), Function.identity(), (a,b) -> a));

            for (InnerOrderDetail_FromOrderRequest each : odRequest) {
                OrderDetail oldDetail = oldDetailsMap.get(each.getProductSku().getId());
                boolean keepNew = true; 

                if (oldDetail != null) {
                    boolean isUnchanged = oldDetail.getQuantity() == each.getQuantity();
                    if (isUnchanged) {
                        keepNew = false;
                    } else {
                        keepNew = true;
                    }
                }
                if (keepNew) {
                    OrderDetail newDetailEntity = OrderDetail.builder()
                        .productId(each.getProduct().getId())
                        .productSkuId(each.getProductSku().getId())
                        .quantity(each.getQuantity())
                        .order(orderDB)
                        .build();
                    if (oldDetail != null) {
                        newDetailEntity.setId(oldDetail.getId()); 
                    }
                    finalDetailsMap.put(
                        each.getProductSku().getId(), 
                        newDetailEntity
                    );
                }
            }

            for (Map.Entry<UUID, OrderDetail> oldEntry : oldDetailsMap.entrySet()) {
                if (!newDetailsMap.containsKey(oldEntry.getKey())) {
                    OrderDetail odValueFromDB = oldEntry.getValue();

                    // Kiểm tra order detail cần xóa
                    if(finalDetailsMap.containsKey(odValueFromDB.getProductSkuId())){ 
                        finalDetailsMap.remove(odValueFromDB.getProductSkuId()); 
                    }
                    detailsToDelete.add(odValueFromDB.getId());

                    inventoryUsageChangeMap.put(odValueFromDB.getProductSkuId(), 
                        ReturnAvailableQuantity.builder()
                        .productId(odValueFromDB.getProductId())
                        .productSkuId(odValueFromDB.getProductSkuId())
                        .circulationCount(odValueFromDB.getQuantity())
                        .isNegative(false) // diff < 0 (old < new) -> Decrease stock (Negative)
                        .build()
                    );

                    kafkaEventData.getPromotions().merge(
                        odValueFromDB.getProductSkuId(), 
                        odValueFromDB.getQuantity(), 
                        Integer::sum
                    );
                }
            }

            if(finalDetailsMap.size() > 0){
                InnerInternalProductRequest productRequest = InnerInternalProductRequest.builder()
                    .productIdList(finalDetailsMap.values().stream().map(OrderDetail::getProductId).distinct().toList())
                    .productSkuIdList(finalDetailsMap.values().stream().map(OrderDetail::getProductSkuId).distinct().toList())
                    .build();
                
                CompletableFuture<List<ProductResponse>> productFuture = (productRequest != null)
                    ? AsyncUtils.fetchAsync(() -> this.productClient.getInternalProductAndProductSkuById(productRequest))
                    : CompletableFuture.completedFuture(null);
                
                CompletableFuture<Void> inventoryFuture = (!odRequest.isEmpty()) 
                    ? AsyncUtils.fetchAsync(() -> this.inventoryClient.checkQuantityAvailableInventoryByProductSkuId(odRequest))
                    : CompletableFuture.completedFuture(null);

                try {
                    CompletableFuture.allOf(productFuture, inventoryFuture).join();
                } catch (CompletionException e) {
                    if (e.getCause() instanceof ServiceException serviceException) {
                        throw serviceException;
                    }
                    throw e;
                }    
                Map<UUID, ProductResponse> productResponse = productFuture.join().stream().collect(Collectors.toMap(ProductResponse::getId, Function.identity(), (a,b) -> b));

                for (OrderDetail orderD : finalDetailsMap.values()) {
                    OrderDetail orderDetailDB = oldDetailsMap.getOrDefault(orderD.getProductSkuId(), null);
                    OrderDetail entityToSave;

                    // Condition update / create / skip
                    int oldQuantity = (orderDetailDB != null) ? orderDetailDB.getQuantity() : 0;
                    int newQuantity = orderD.getQuantity();
                    int diff = oldQuantity - newQuantity;

                    boolean isQuantityChanged = diff != 0;
                    boolean isNewEntity = orderD.getId() == null; // Create position

                    ProductResponse product = productResponse.get(orderD.getProductId());
                    InnerProductSkuResponse productSkuResponse = product.getProductSkus().stream().filter(p -> p.getId().equals(orderD.getProductSkuId())).findFirst().orElseGet(null);

                    // ADD PROMOTION DATA TO KAFKA. Promotion has been decreased after resolving negative quantity and opposite.
                    if(isQuantityChanged){
                        kafkaEventData.getPromotions().merge(
                            orderD.getProductSkuId(), 
                            diff, 
                            Integer::sum
                        );
                    }

                    // Tính toán số lượng sản phẩm của đơn thay đổi
                    if (isNewEntity) {
                        // TRƯỜNG HỢP A: THÊM MỚI HOÀN TOÀN (chưa có ID, diff = 0 - newQuantity)
                        inventoryUsageChangeMap.put(orderD.getProductSkuId(), 
                            ReturnAvailableQuantity.builder()
                            .productId(orderD.getProductId())
                            .productSkuId(productSkuResponse.getId())
                            .circulationCount(newQuantity)
                            .isNegative(true) // Auto decrease inventory quantity available
                            .build()
                        );
                    } else if (isQuantityChanged) {
                        // TRƯỜNG HỢP B: SỬA ĐỔI SỐ LƯỢNG (Có ID và diff != 0)
                        inventoryUsageChangeMap.put(orderD.getProductSkuId(), 
                            ReturnAvailableQuantity.builder()
                            .productId(orderD.getProductId())
                            .productSkuId(productSkuResponse.getId())
                            .circulationCount(Math.abs(diff))
                            .isNegative(diff < 0) // diff < 0 (old < new) -> Decrease inventory quantity available (Negative) and opposite
                            .build()
                        );
                    }

                    // TÍNH TOÁN CHO CHI TIẾT ĐƠN HÀNG (OrderDetail)
                    if (orderD.getQuantity() == null || orderD.getQuantity() <= 0) continue;
                    BigDecimal qty = BigDecimal.valueOf(orderD.getQuantity());

                    BigDecimal originalPricePerUnit = productSkuResponse.getPrice();
                    BigDecimal discountPerUnit = productSkuResponse.getPromotion() != null ? productSkuResponse.getPromotion().getDiscountFinal() : BigDecimal.ZERO;
                    BigDecimal finalPricePerUnit = originalPricePerUnit.subtract(discountPerUnit);

                    BigDecimal totalPrice_Detail = calculateTotalPrice(originalPricePerUnit,discountPerUnit,qty);

                    if (orderDetailDB != null) {
                        // TRƯỜNG HỢP CẬP NHẬT: Dùng chính thằng DB để Hibernate không báo lỗi detached
                        entityToSave = orderDetailDB;
                    } else {
                        // TRƯỜNG HỢP THÊM MỚI: Dùng thằng orderD (thằng này ID đang null nên persist thoải mái)
                        entityToSave = orderD;
                    }

                    entityToSave.setActivated(true);
                    entityToSave.setPromotionDiscount(discountPerUnit);
                    entityToSave.setPriceOriginal(originalPricePerUnit);
                    entityToSave.setPrice(finalPricePerUnit);
                    entityToSave.setQuantity(orderD.getQuantity()); // Giữ nguyên
                    entityToSave.setTotalPrice(totalPrice_Detail);
                    entityToSave.setProductId(product.getId());
                    entityToSave.setProductSkuId(productSkuResponse.getId());
                    entityToSave.setOrder(orderDB);

                    updatedOrderDetails.add(entityToSave);

                    totalItemPrice_Order = totalItemPrice_Order.add(originalPricePerUnit.multiply(qty));
                    totalDiscountByPromotion = totalDiscountByPromotion.add(discountPerUnit.multiply(qty));
                    totalItem_Order += orderD.getQuantity();
                }
            }
            if (orderDB.getOrderDetails() == null) {
                orderDB.setOrderDetails(new ArrayList<>());
            }
            // Clear all relationship Order detail from Order to prevent Hibernate referenced exception
            orderDB.getOrderDetails().clear();
            orderDB.getOrderDetails().addAll(updatedOrderDetails);
            orderDB.setCoupon(couponCurrentUp);

            if(couponCurrentUp != null){
                couponAmount = 
                    couponCurrentUp.getType().equals(CouponEnum.PERCENT) ? 
                    totalItemPrice_Order.multiply(couponCurrentUp.getCouponAmount()).divide(BigDecimal.valueOf(100),0, RoundingMode.HALF_UP) : 
                    couponCurrentUp.getCouponAmount();
                
            }
            totalDiscount = totalDiscountByPromotion.add(couponAmount);

            orderReq.setTotalPrice(totalItemPrice_Order);
            orderReq.setDiscountPrice(totalDiscount);
            orderReq.setTotalItem(totalItem_Order);

            // ADD INVENTORY DATA TO KAFKA
            kafkaEventData.setInventories(inventoryUsageChangeMap.values());

            // Calculate shipping free
            if(isShippingInfoChanged){
                // ADD SHIPPING DATA TO KAFKA
                kafkaEventData.setShipping(
                    ShippingResponse.builder()
                    .status(orderDB.getShippingStatus())
                    .orderId(orderDB.getId())
                    .id(orderDB.getShippingId())
                    .build()
                );
                orderReq.setShippingFee(BigDecimal.ZERO);
                finalPrice = totalItemPrice_Order.subtract(totalDiscount);
            } else {
                BigDecimal oldShippingFee = orderDB.getShippingFee() != null ? orderDB.getShippingFee() : BigDecimal.ZERO;
                finalPrice = totalItemPrice_Order.subtract(totalDiscount.add(oldShippingFee));
            }
            orderReq.setFinalPrice(finalPrice);

            // Áp dụng các thay đổi địa chỉ phép vào oldData
            this.applyAllowedChanges(orderDB, orderReq, changedFields, IMPORTANT_FIELDS_STATUS_PENDING);


            if (changedFields.containsKey("paymentMethod")) {
                kafkaEventData.setPayment(
                    InnerInternalPayment.builder()
                    .id(orderDB.getPaymentId())
                    .status(orderDB.getPaymentStatus())
                    .amount(orderDB.getFinalPrice())
                    .paymentMethod(orderReq.getPaymentMethod())
                    .orderId(orderDB.getId())
                    .build()
                );
            }
            return kafkaEventData;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [updateOrderStatusPending] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private Map<String, Object[]> detectChangedFields(Order oldData, Order newData, Set<String> ignoredFields) {
        Map<String, Object[]> changes = new HashMap<>();
        try {
            for (Field field : Order.class.getDeclaredFields()) {
                if(ignoredFields.contains(field.getName())){
                    continue;
                }
                field.setAccessible(true);
                Object oldValue = field.get(oldData);
                Object newValue = field.get(newData);
                
                if (!Objects.equals(oldValue, newValue)) {
                    changes.put(field.getName(), new Object[]{oldValue, newValue});
                }
            }
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [detectChangedFields] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }

        return changes;
    }

    private void applyAllowedChanges(Order oldData, Order newData, Map<String, Object[]> changedFields, Set<String> allowedFields ) throws IllegalAccessException {
        for (String fieldName : changedFields.keySet()) {
            if (allowedFields.contains(fieldName)) {
                try {
                    // Dùng Reflection an toàn:
                    Field field = Order.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(oldData, field.get(newData));
                } catch (Exception e) {
                    log.warn("[applyAllowedChanges] Field not found: {}", fieldName, e);
                }
            }
        }
    }
}
