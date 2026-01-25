package com.fashion.order.service.impls;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import com.fashion.order.dto.response.internal.ProductResponse;
import com.fashion.order.dto.response.internal.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.order.dto.response.internal.UserResponse;
import com.fashion.order.dto.response.internal.AddressResponse.InnerAddressResponse;
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
        "paymentMethod",
        "receiverName",
        "receiverEmail",
        "receiverPhone",
        "totalItem",
        "totalPrice",
        "discountPrice",
        "finalPrice"
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
        "paymentStatus",
        "shippingStatus",
        "status",
        "shippingFee",
        "shippingId",
        "paymentId",
        "note",
        "orderDetails",
        "coupon",
        "code",
        "activated",
        "version",
        "totalItem",
        "totalPrice",
        "discountPrice",
        "finalPrice"
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

            // KAFKA SEND EVENT DECREASE: INVENTORY, PROMOTION. AND CREATE PAYMENT TRANSACTION, SHIPPING.
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
            if(order.getStatus().equals(OrderEnum.PENDING)){
                this.updateOrderStatusPending(order, saveOrder, odRequest);
            } else if (order.getStatus().equals(OrderEnum.CONFIRMED)) {
                // this.updateOrderStatusConfirmed(order, saveOrder, odRequest);
            }
            return null;
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

    private String generateUniqueOrderCode() {
        final int MAX_ATTEMPTS = 5; 
        try {
            for (int i = 0; i < MAX_ATTEMPTS; i++) {
                String prefix = "HD" + LocalDate.now().getYear(); 
                String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase(); 
                String orderCode = prefix + "-" + randomPart;
                Order order = this.orderRepository.findByCode(orderCode).orElseGet(null);
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
    

    private OrderResponse updateOrderStatusPending(Order orderDB, Order orderReq, List<InnerOrderDetail_FromOrderRequest> odRequest){
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
            BigDecimal totalItemPrice_Order = BigDecimal.ZERO; // Tổng giá gốc (priceOriginal * quantity)
            BigDecimal totalDiscountByPromotion = BigDecimal.ZERO; // Tổng giảm giá từ Promotion (discountFinal * quantity)
            BigDecimal couponAmount = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;
            Integer totalItem_Order = 0;
            BigDecimal finalPrice = orderDB.getFinalPrice();

            Map<Long, Integer> promotionUsageChangeMap = new HashMap<>();
            
            List<Long> detailsToDelete = new ArrayList<>();
            Map<UUID, OrderDetail> finalDetailsMap = new HashMap<>(oldDetailsMap);
            
            return null;
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
