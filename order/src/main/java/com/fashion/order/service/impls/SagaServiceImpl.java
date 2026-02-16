package com.fashion.order.service.impls;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.order.common.enums.SagaStateStatusEnum;
import com.fashion.order.common.enums.SagaStateStepEnum;
import com.fashion.order.dto.response.SagaStateResponse;
import com.fashion.order.dto.response.internal.InventoryResponse.ReturnAvailableQuantity;
import com.fashion.order.dto.response.internal.PaymentResponse.InnerInternalPayment;
import com.fashion.order.dto.response.kafka.OrderCreatedEvent;
import com.fashion.order.entity.Order;
import com.fashion.order.entity.SagaState;
import com.fashion.order.messaging.provider.OrderServiceProvider;
import com.fashion.order.repository.OrderRepository;
import com.fashion.order.repository.SagaStateRepository;
import com.fashion.order.service.KafkaService;
import com.fashion.order.service.SagaStateService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SagaServiceImpl implements SagaStateService {
    OrderServiceProvider orderServiceProvider;
    SagaStateRepository sagaStateRepository;
    OrderRepository orderRepository;
    /**
     * 
     * @param thenCompose (Saga Orchestration)
     * @return dùng để xâu chuỗi các tác vụ bất đồng bộ phụ thuộc lẫn nhau. 
     * @return giúp viết code theo kiểu "phẳng" (pipeline). Task A xong thì lấy kết quả của Task A để chạy Task B.
     */
    // @Transactional
    @Override
    public CompletableFuture<SagaStateResponse> executeOrderSaga(OrderCreatedEvent orderCreatedEvent, Order order){
        UUID sagaId = UUID.randomUUID();
        Instant createdAt = order.getCreatedAt();
        UUID orderId = orderCreatedEvent.getPayment().getOrderId();

        log.info("ORDER-SERVICE: [executeOrderSaga] Starting saga {} for order {}", sagaId, orderId);

        SagaState sagaState = SagaState.builder()
            .id(sagaId)
            .orderId(orderId)
            .orderCode(order.getCode())
            .orderCreatedAt(createdAt)
            .step(SagaStateStepEnum.PAYMENT)
            .status(SagaStateStatusEnum.START)
            .payload(orderCreatedEvent.toString())
            .createdAt(createdAt)
            .build();
        
        sagaStateRepository.save(sagaState);
        sagaStateRepository.flush();
         return executePaymentStep(sagaState, orderCreatedEvent.getPayment())
            .orTimeout(35, TimeUnit.SECONDS)
            // .exceptionally(ex -> {
            //     if (ex instanceof TimeoutException) {
            //         log.error("ORDER-SERVICE: Saga Orchestration: Payment step timeout for saga {}", sagaId);
            //         return SagaStateResponse.failure("Payment step timeout");
            //     }
            //     throw new CompletionException(ex);
            // })
            .thenCompose(result -> {
                if (!result.isSuccess()) {
                    log.warn("ORDER-SERVICE: Payment step failed for saga {}", sagaId);
                    return CompletableFuture.completedFuture(result);
                }
                
                sagaState.setStep(SagaStateStepEnum.PROMOTION);
                sagaState.setPayload(orderCreatedEvent.getPayment().toString());
                sagaStateRepository.save(sagaState);
                sagaStateRepository.flush();

                order.setPaymentId(result.getPaymentId());
                order.setPaymentStatus(result.getPaymentStatus());
                orderRepository.updatePaymentId(order.getId(), createdAt, result.getPaymentId(), result.getPaymentStatus());
                orderRepository.flush();
                return executePromotionStep(sagaState, orderCreatedEvent.getPromotions());
            })
            .orTimeout(35, TimeUnit.SECONDS)
            .thenCompose(result -> {
                if (!result.isSuccess()) {
                    log.warn("ORDER-SERVICE: Promotion step failed for saga {}, compensating payment", sagaId);
                    return compensatePayment(sagaState, orderCreatedEvent.getPayment())
                        .thenApply(v -> result);
                }
                
                sagaState.setStep(SagaStateStepEnum.INVENTORY);
                sagaState.setPayload(orderCreatedEvent.getInventories().toString());
                sagaStateRepository.save(sagaState);
                sagaStateRepository.flush();
                return executeInventoryStep(sagaState, orderCreatedEvent.getInventories());
            })
            .orTimeout(35, TimeUnit.SECONDS)
            .thenCompose(result -> {
                if (!result.isSuccess()) {
                    log.warn("ORDER-SERVICE: Inventory step failed for saga {}, compensating all", sagaId);
                    return compensatePromotion(sagaState, orderCreatedEvent.getPromotions())
                        .thenCompose(v -> compensatePayment(sagaState, orderCreatedEvent.getPayment()))
                        .thenApply(v -> result);
                }
                
                sagaState.setStatus(SagaStateStatusEnum.COMPLETED);
                sagaStateRepository.save(sagaState);
                log.info("ORDER-SERVICE: Saga {} completed successfully", sagaId);
                
                return CompletableFuture.completedFuture(result);
            })
            .exceptionally(ex -> {
                log.error("ORDER-SERVICE: Saga {} failed with exception", sagaId, ex);
                sagaState.setStatus(SagaStateStatusEnum.FAILED);
                sagaState.setPayload(ex.getMessage());
                sagaStateRepository.save(sagaState);

                compensateAll(sagaState, orderCreatedEvent).join();

                return SagaStateResponse.failure(ex.getMessage());
            });
    }

    private CompletableFuture<SagaStateResponse> executePaymentStep(
        SagaState sagaState, 
        InnerInternalPayment payment
    ) {
        log.info("ORDER-SERVICE: [executePaymentStep] Executing PAYMENT step for saga {}", sagaState.getId());
        
        try {
            return orderServiceProvider.produceOrderCreatedEventSuccessPayment(sagaState,payment);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [executePaymentStep] Error executing PAYMENT step", e);
            return CompletableFuture.completedFuture(
                SagaStateResponse.failure("PAYMENT step failed: " + e.getMessage())
            );
        }
    }    
    
    private CompletableFuture<SagaStateResponse> executePromotionStep(SagaState sagaState, Map<UUID, Integer> promotions){
        log.info("ORDER-SERVICE: [executePromotionStep] Executing PROMOTION step for saga {}", sagaState.getId());
        try {
            return this.orderServiceProvider.produceOrderCreatedEventSuccessPromotion(sagaState, promotions);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [executePromotionStep] Error executing PROMOTION step", e);
            return CompletableFuture.completedFuture(
                SagaStateResponse.failure("PROMOTION step failed: " + e.getMessage())
            );
        }
    }
    
    private CompletableFuture<SagaStateResponse> executeInventoryStep(SagaState sagaState, Collection<ReturnAvailableQuantity> inventories){
        log.info("ORDER-SERVICE: [executeInventoryStep] Executing INVENTORY step for saga {}", sagaState.getId());
        try {
            return this.orderServiceProvider.produceOrderCreatedEventSuccessInventory(sagaState, inventories);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [executeInventoryStep] Error executing INVENTORY step", e);
            return CompletableFuture.completedFuture(
                SagaStateResponse.failure("INVENTORY step failed: " + e.getMessage())
            );
        }
    }

    // ===== COMPENSATION STEPS =====
    private CompletableFuture<Void> compensatePayment(
        SagaState sagaState,
        InnerInternalPayment payment
    ) {
        log.warn("ORDER-SERVICE: [compensatePayment] Compensating PAYMENT for saga {}", sagaState.getId());
        
        try {
            return orderServiceProvider.produceOrderCreatedEventSuccessPaymentFailed(sagaState, payment);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [compensatePayment] Error compensating payment", e);
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private CompletableFuture<Void> compensatePromotion(
        SagaState sagaState,
        Map<UUID, Integer> promotions
    ) {
        log.warn("ORDER-SERVICE: [compensatePromotion] Compensating PROMOTION for saga {}", sagaState.getId());
        
        try {
            return orderServiceProvider.produceOrderCreatedEventSuccessPromotionFailed(sagaState, promotions);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [compensatePromotion] Error compensating promotion", e);
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private CompletableFuture<Void> compensateInventory(
        SagaState sagaState,
        Collection<ReturnAvailableQuantity> inventories
    ) {
        log.warn("ORDER-SERVICE: [compensateInventory] Compensating INVENTORY for saga {}", sagaState.getId());
        
        try {
            return orderServiceProvider.produceOrderCreatedEventSuccessInventoryFailed(sagaState, inventories);
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [compensateInventory] Error compensating inventory", e);
            return CompletableFuture.completedFuture(null);
        }
    }


    private CompletableFuture<Void> compensateAll(SagaState sagaState, OrderCreatedEvent orderCreatedEvent) {
        SagaStateStepEnum currentStep = sagaState.getStep();
        
        List<CompletableFuture<Void>> compensations = new ArrayList<>();
        
        if (currentStep.ordinal() >= SagaStateStepEnum.INVENTORY.ordinal()) {
            compensations.add(compensateInventory(sagaState, orderCreatedEvent.getInventories()));
        }
        if (currentStep.ordinal() >= SagaStateStepEnum.PROMOTION.ordinal()) {
            compensations.add(compensatePromotion(sagaState, orderCreatedEvent.getPromotions()));
        }
        if (currentStep.ordinal() >= SagaStateStepEnum.PAYMENT.ordinal()) {
            compensations.add(compensatePayment(sagaState, orderCreatedEvent.getPayment()));
        }
        
        return CompletableFuture.allOf(compensations.toArray(new CompletableFuture[0]));
    }
}
