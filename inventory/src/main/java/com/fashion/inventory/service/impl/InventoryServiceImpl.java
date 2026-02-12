package com.fashion.inventory.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.inventory.common.enums.EnumError;
import com.fashion.inventory.common.enums.InventoryTransactionReferenceTypeEnum;
import com.fashion.inventory.common.enums.InventoryTransactionTypeEnum;
import com.fashion.inventory.common.enums.WareHouseStatusEnum;
import com.fashion.inventory.common.response.ApiResponse;
import com.fashion.inventory.common.util.AsyncUtils;
import com.fashion.inventory.common.util.MessageUtil;
import com.fashion.inventory.common.util.PageableUtils;
import com.fashion.inventory.common.util.SpecificationUtils;
import com.fashion.inventory.dto.request.InventoryRequest;
import com.fashion.inventory.dto.request.InventoryRequest.BaseInventoryRequest;
import com.fashion.inventory.dto.request.InventoryRequest.InnerOrderDetail_FromOrderRequest;
import com.fashion.inventory.dto.request.InventoryRequest.ReturnAvailableQuantity;
import com.fashion.inventory.dto.request.search.SearchModel;
import com.fashion.inventory.dto.request.search.SearchOption;
import com.fashion.inventory.dto.request.search.SearchRequest;
import com.fashion.inventory.dto.response.InventoryResponse;
import com.fashion.inventory.dto.response.PaginationResponse;
import com.fashion.inventory.dto.response.WareHouseResponse;
import com.fashion.inventory.dto.response.internal.ApprovalHistoryResponse;
import com.fashion.inventory.dto.response.internal.ProductResponse;
import com.fashion.inventory.dto.response.internal.ProductSkuResponse;
import com.fashion.inventory.dto.response.internal.RoleResponse;
import com.fashion.inventory.dto.response.internal.UserResponse;
import com.fashion.inventory.dto.response.internal.ProductResponse.InnerProductResponse;
import com.fashion.inventory.dto.response.internal.ProductSkuResponse.InnerProductSkuResponse;
import com.fashion.inventory.dto.response.internal.UserResponse.UserInsideToken;
import com.fashion.inventory.dto.response.kafka.ProductApprovedEvent;
import com.fashion.inventory.entity.Inventory;
import com.fashion.inventory.entity.InventoryTransaction;
import com.fashion.inventory.entity.WareHouse;
import com.fashion.inventory.exception.ServiceException;
import com.fashion.inventory.intergration.IdentityClient;
import com.fashion.inventory.intergration.ProductClient;
import com.fashion.inventory.mapper.InventoryMapper;
import com.fashion.inventory.properties.cache.InventoryServiceCacheProperties;
import com.fashion.inventory.repository.InventoryRepository;
import com.fashion.inventory.repository.InventoryTransactionRepository;
import com.fashion.inventory.repository.WareHouseRepository;
import com.fashion.inventory.security.SecurityUtils;
import com.fashion.inventory.service.InventoryService;
import com.fashion.inventory.service.WareHouseService;
import com.fashion.inventory.service.provider.CacheProvider;
import com.fashion.inventory.service.provider.WareHouseUpSertOrderErrorProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class InventoryServiceImpl implements InventoryService {
    final InventoryRepository inventoryRepository;
    final InventoryMapper inventoryMapper;
    final IdentityClient identityClient;
    final ProductClient productClient;
    final WareHouseRepository wareHouseRepository;
    final MessageUtil messageUtil;
    final InventoryTransactionRepository inventoryTransactionRepository;
    final WareHouseUpSertOrderErrorProvider wareHouseUpSertOrderErrorProvider;
    final EntityManager entityManager;
    final Executor virtualExecutor;
    final WareHouseService wareHouseService;
    final InventoryServiceCacheProperties inventoryServiceCacheProperties;
    final CacheProvider cacheProvider;
    @Value("${role.admin}")
    String roleAdmin;
    
    @Value("${role.seller}")
    String roleSeller;
    
    @Override
    public List<InventoryResponse> findRawListInventoryBySku(List<UUID> skuIds) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawListInventoryBySku'");
    }

    @Override
    public boolean existsByProductSkuId(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsByProductSkuId'");
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public List<UUID> adjustmentsStockAfterProductApproved(List<ProductApprovedEvent> events, UUID eventId) {
        log.info("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Start import/adjustment inventory");
        try {
            if (events == null || events.isEmpty())
                throw new ServiceException(EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_ID, "inventory.transaction.exist.eventId");
            if(this.inventoryTransactionRepository.existsByEventIdAndReferenceTypeAndReferenceId(eventId,InventoryTransactionReferenceTypeEnum.PRODUCT,events.getFirst().getProductId())){
                log.warn("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Skip duplicated product approved eventId, productId={}", events.getFirst().getProductId(), eventId);
                throw new ServiceException(EnumError.INVENTORY_INVENTORY_TRANSACTION_DATA_EXISTED_EVENT_ID, "inventory.transaction.exist.eventId");
            }
            Locale locale = Locale.ENGLISH;
            List<UUID> skuIds = events.stream()
                .map(ProductApprovedEvent::getProductSkuId)
                .distinct()
                .toList();
            Map<UUID, Inventory> inventoryMap = inventoryRepository
                .lockInventoryBySkuId(skuIds)
                .stream()
                .collect(Collectors.toMap(
                    Inventory::getProductSkuId,
                    Function.identity(),
                    (a, b) -> a
                ));
            
            WareHouse defaultWareHouse = this.wareHouseRepository.findFirstByStatusOrderByCreatedAtDesc(WareHouseStatusEnum.ACTIVE);
            if (defaultWareHouse == null) {
                throw new ServiceException(
                    EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_STATUS,
                    "ware.house.notExisted.status.active"
                );
            }
            
            List<Inventory> inventoriesToSave = new ArrayList<>();
            List<InventoryTransaction> transactionsToSave = new ArrayList<>();
            for (ProductApprovedEvent event : events) {
                UUID skuId = event.getProductSkuId();
                Inventory inventory = inventoryMap.get(skuId);
                boolean isNewInventory = inventory == null;

                WareHouse wareHouse;
                if (isNewInventory) {
                    wareHouse = defaultWareHouse;
                } else {
                    if (inventory == null || inventory.getWareHouse() == null) {
                        throw new ServiceException(
                            EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_STATUS,
                            "ware.house.notExisted.status.active"
                        );
                    }
                    wareHouse = inventory.getWareHouse();
                }

                int before = 0;
                int after = 0;
                
                if (isNewInventory) {
                    before = 0;
                    after = event.getQuantityAvailable();
                    
                    this.validateQuantity(after, skuId);

                    inventory = Inventory.builder()
                        .activated(true)
                        .productId(event.getProductId())
                        .productSkuId(skuId)
                        .quantityAvailable(after)
                        .quantityReserved(0)
                        .quantitySold(0)
                        .wareHouse(wareHouse)
                        .build();
                } else {
                    before = inventory.getQuantityAvailable() == null
                        ? 0
                        : inventory.getQuantityAvailable();

                    after = before + event.getQuantityAvailable();
                    this.validateQuantity(after, skuId);

                    inventory.setActivated(true);
                    inventory.setQuantityAvailable(after);
                }
                inventoriesToSave.add(inventory);

                InventoryTransactionTypeEnum type = isNewInventory
                    ? InventoryTransactionTypeEnum.IMPORT
                    : InventoryTransactionTypeEnum.ADJUSTMENT;

                String messageKey = isNewInventory
                    ? "inventory.transaction.import.product"
                    : "inventory.transaction.adjustment.product";

                String note = messageUtil.getMessage(messageKey, skuId, locale);
                // Build transaction
                transactionsToSave.add(InventoryTransaction.builder()
                    .activated(true)
                    .productSkuId(skuId)
                    .beforeQuantity(before)
                    .afterQuantity(after)
                    .quantityChange(event.getQuantityAvailable())
                    .type(type)
                    .referenceType(InventoryTransactionReferenceTypeEnum.PRODUCT)
                    .referenceId(event.getProductId())
                    .note(note)
                    .eventId(eventId)
                    .wareHouse(wareHouse)
                    .build()
                );
            }
            if(!inventoriesToSave.isEmpty()){
                this.inventoryRepository.saveAll(inventoriesToSave);
            }
            if(!transactionsToSave.isEmpty()){
                this.inventoryTransactionRepository.saveAll(transactionsToSave);
            }
            return skuIds;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public InventoryResponse createInventory(InventoryRequest inventory) {
        log.info("INVENTORY-SERVICE: [createInventory] Start create inventory");
        try {
            InventoryResponse inventoryResponse = this.upSertInventory(inventory, true);
            this.updateInventoryCache(inventoryResponse);
            return inventoryResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [createInventory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public InventoryResponse updateInventory(InventoryRequest inventory) {
        log.info("INVENTORY-SERVICE: [createInventory] Start create inventory");
        try {
            InventoryResponse inventoryResponse = this.upSertInventory(inventory, false);
            this.updateInventoryCache(inventoryResponse);
            return inventoryResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [createInventory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    // @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id, Long version) {
        try {
            String cacheKey = this.getCacheKey(id);
            String lockKey = this.getLockKey(id);
            return cacheProvider.getDataResponse(cacheKey, lockKey, version, InventoryResponse.class, () ->
                this.inventoryRepository.findById(id)
                    .map((i) -> {
                        CompletableFuture<ApiResponse<ProductResponse>> productFuture = (i.getProductId() != null)
                            ? AsyncUtils.fetchAsyncWThread(() -> this.productClient.getInternalProductByProductId(i.getProductId(), version), virtualExecutor)
                            : CompletableFuture.completedFuture(null);
                        CompletableFuture<InventoryResponse> inventoryFuture = AsyncUtils.fetchAsyncWThread(() -> this.inventoryMapper.toDto(i), virtualExecutor);
                        
                        try {
                            CompletableFuture.allOf(
                                productFuture, 
                                inventoryFuture
                            ).join();
                        } catch (CompletionException e) {
                            if (e.getCause() instanceof ServiceException serviceException) {
                                throw serviceException;
                            }
                            throw e;
                        }
                        InventoryResponse inventoryResponse = inventoryFuture.join();
                        ProductResponse productResponse = productFuture.join().getData();

                        InnerProductSkuResponse foundSku = null;
                        if (productResponse != null && productResponse.getProductSkus() != null) {
                            inventoryResponse.setProductSku(productResponse.getProductSkus().stream()
                                .filter(p -> i.getProductSkuId().equals(p.getId()))
                                .findFirst()
                                .map(p -> InnerProductSkuResponse.builder()
                                    .id(p.getId())
                                    .sku(p.getSku())
                                    .price(p.getPrice())
                                    .tempStock(p.getTempStock())
                                    .build())
                                .orElse(null)
                            );
                        }

                        if (productResponse != null) {
                            inventoryResponse.setProduct(InnerProductResponse.builder()
                            .id(productResponse.getId())
                            .name(productResponse.getName())
                            .build());
                        }

                        inventoryResponse.setVersion(System.currentTimeMillis());
                        return inventoryResponse;
                    })
                    .orElseThrow(
                        () -> new ServiceException(
                            EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_ID,
                            "inventory.not.found.id",
                            Map.of("id", id)
                )
                    )
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [getInventoryById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    // @Transactional(readOnly = true)
    public PaginationResponse<List<InventoryResponse>> getAllInventories(SearchRequest request) {
        try {
            SearchOption searchOption = request.getSearchOption();
            SearchModel searchModel = request.getSearchModel();

            List<String> allowedField = List.of("createdAt");

            PageRequest pageRequest = PageableUtils.buildPageRequest(
                searchOption.getPage(), 
                searchOption.getSize(), 
                searchOption.getSort(), 
                allowedField, 
                "createdAt", 
                Sort.Direction.DESC
            );
            List<String> fields = SpecificationUtils.getFieldsSearch(Inventory.class);

            Specification<Inventory> spec = new SpecificationUtils<Inventory>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Inventory> inventories = this.inventoryRepository.findAll(spec, pageRequest);
            
            ApiResponse<List<ProductSkuResponse>> productSkusFuture = this.productClient.getInternalProductSkuByIds(
                inventories.getContent().stream().map(Inventory::getProductSkuId).distinct().toList()
            );
            List<InventoryResponse> inventoryResponses = this.inventoryMapper.toDto(inventories.getContent());
            if(!productSkusFuture.getData().isEmpty()){
                Map<UUID, ProductSkuResponse> mapProductSku = productSkusFuture.getData().stream().collect(Collectors.toMap(ProductSkuResponse::getId, Function.identity(),(a,b) -> a));
                inventoryResponses = inventoryResponses.stream().map(
                    i -> {
                        ProductSkuResponse p = mapProductSku.getOrDefault(i.getProductSkuId(), null);
                        if(p != null){
                            i.setProductSku(
                                InnerProductSkuResponse.builder()
                                .id(p.getId())
                                .sku(p.getSku())
                                .price(p.getPrice())
                                .tempStock(p.getTempStock())
                                .build()
                            );
                            i.setProduct(
                                InnerProductResponse.builder()
                                .id(p.getProduct().getId())
                                .name(p.getProduct().getName())
                                .build()
                            );
                        }
                        return i;
                    }
                ).toList();
            }
            return PageableUtils.<Inventory, InventoryResponse>buildPaginationResponse(pageRequest, inventories, inventoryResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [getAllInventories] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public List<Inventory> findRawInventoriesByProductId(UUID productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawInventoriesByProductId'");
    }

    @Override
    public Inventory findRawInventoryByProductIdAndProductSkuId(UUID productId, UUID productSkuId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findRawInventoryByProductIdAndProductSkuId'");
    }
    
    @Override
    @Transactional(readOnly = true)
    public void checkInternalQuantityAvailableForOrder(Collection<InnerOrderDetail_FromOrderRequest> requests) {
        try {
            Map<UUID, Integer> mapRequest = requests.stream()
                .collect(Collectors.toMap(
                    i -> i.getProductSku().getId(), 
                    InnerOrderDetail_FromOrderRequest::getQuantity, 
                    Integer::sum
                ));

            List<Inventory> inventories = this.inventoryRepository.findAllByProductSkuIdIn(mapRequest.keySet());
            
            // Finding SKU not existed in Ware house record
            if (inventories.size() != mapRequest.size()) {
                // Getting exact SKU name which lack in Warehouse to throw error
                Set<UUID> foundSkuIds = inventories.stream().map(Inventory::getProductSkuId).collect(Collectors.toSet());
                List<UUID> missingSkuIds = mapRequest.keySet().stream().filter(id -> !foundSkuIds.contains(id)).toList();
                throw new ServiceException(EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_SKU_ID, "inventory.not.found.productSkuId" + missingSkuIds);
            }

            // 4. Validate
            for (Inventory inventory : inventories) {
                Integer requestedQty = mapRequest.get(inventory.getProductSkuId());
                Integer stockAvailable = inventory.getQuantityAvailable();

                if (requestedQty <= 0 || requestedQty > stockAvailable) {
                    throw new ServiceException(
                        EnumError.INVENTORY_INVENTORY_INVALID_QUANTITY_AVAILABLE, 
                        "inventory.quantityAvailable.stock.out", 
                        Map.of("skuId", inventory.getProductSkuId(), "available", stockAvailable)
                    );
                }

                if (inventory.getWareHouse() != null && inventory.getWareHouse().getStatus() != null) {
                    inventory.getWareHouse().getStatus().validateOrderAbility(
                        wareHouseUpSertOrderErrorProvider, 
                        Map.of("productSkuId", inventory.getProductSkuId())
                    );
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [checkInternalQuantityAvailableForOrder] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteInventoryById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteInventoryById'");
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void changeQuantityUse(Collection<ReturnAvailableQuantity> request, UUID eventId) {
        try {
            // Prevent Idempotency error from Kafka
            if (this.inventoryTransactionRepository.existsByEventId(eventId)) {
                log.warn("INVENTORY-SERVICE: Event {} already processed. Skipping.", eventId);
                return; 
            }
            Set<InventoryTransaction> savedTransactions = new HashSet<>();
            WareHouse activeWh = this.wareHouseRepository.findFirstByStatusOrderByCreatedAtDesc(WareHouseStatusEnum.ACTIVE);
            for (ReturnAvailableQuantity item : request) {
                Inventory currentInventory = (item.isNegative() 
                ? inventoryRepository.decreaseQuantityAndIncreaseReservedAtomic(item.getProductId(), item.getProductSkuId(), item.getCirculationCount())
                : inventoryRepository.increaseQuantityAndDecreaseReservedAtomic(item.getProductId(), item.getProductSkuId(), item.getCirculationCount()))
                .orElseThrow(() -> new ServiceException(
                    EnumError.INVENTORY_INVENTORY_INVALID_QUANTITY_AVAILABLE, 
                    "inventory.quantityAvailable.not.enough.for.atomic.update"
                ));
                
                String note = "";
                Integer afterAvailableQuantity = currentInventory.getQuantityAvailable();
                Integer beforeAvailableQuantity;

                if (item.isNegative()) {
                    // Reserved increase (Decrease Available)
                    beforeAvailableQuantity = afterAvailableQuantity + item.getCirculationCount();
                    note = String.format("Order products: %s (Reserved +%d, Available -%d)", item.getProductSkuId(), item.getCirculationCount(), item.getCirculationCount());
                } else {
                    // Reserved decrease (Increase Available)
                    beforeAvailableQuantity = afterAvailableQuantity - item.getCirculationCount();
                    note = String.format("Return order products: %s (Reserved -%d, Available +%d)", item.getProductSkuId(), item.getCirculationCount(), item.getCirculationCount());
                }

                InventoryTransactionTypeEnum tType = item.isNegative() 
                ? InventoryTransactionTypeEnum.ORDER_RESERVE 
                : InventoryTransactionTypeEnum.ORDER_RELEASE;

                savedTransactions.add(InventoryTransaction.builder()
                    .activated(true)
                    .note(note)
                    .referenceId(item.getProductId())
                    .referenceType(InventoryTransactionReferenceTypeEnum.ORDER)
                    .type(tType)
                    .afterQuantity(afterAvailableQuantity) 
                    .beforeQuantity(beforeAvailableQuantity)
                    .quantityChange(Math.abs(item.getCirculationCount()))
                    .productSkuId(item.getProductSkuId())
                    .eventId(eventId)
                    .wareHouse(activeWh)
                    .build()
                );

            }
            this.inventoryTransactionRepository.saveAll(savedTransactions);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [changeQuantityUse] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    private InventoryResponse upSertInventory(InventoryRequest inventoryRequest, boolean isCreate){
        try {
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, "auth.accessToken.invalid")
            );
            CompletableFuture<WareHouse> wareHouseFuture = AsyncUtils.fetchAsyncWThread(
                () -> this.wareHouseService.fetchWareHouse(inventoryRequest.getWareHouse().getId()), 
                virtualExecutor
            );
            CompletableFuture<Void> userFuture = (currentUser.getId() != null)
                ? AsyncUtils.fetchVoidWThread(() -> identityClient.validateInternalUserById(currentUser.getId(), true, inventoryRequest.getVersion()), virtualExecutor)
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> productValidationFuture = (inventoryRequest.getProductId() != null)
                ? AsyncUtils.fetchVoidWThread(
                    () -> productClient.validateInternalProductById(
                        inventoryRequest.getProductId(), 
                        inventoryRequest.getProductSkuId()
                    ),
                    virtualExecutor
                )
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> approvalValidationFuture = (inventoryRequest.getProductId() != null)
                ? AsyncUtils.fetchVoidWThread(
                    () -> productClient.validateInternalApprovalHistoryByRequestId(
                        inventoryRequest.getProductId()
                    ),
                    virtualExecutor
                )
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> duplicateCheckFuture = AsyncUtils.fetchVoidWThread(
                () -> this.checkExistedInventory(
                    inventoryRequest.getProductId(), 
                    inventoryRequest.getProductSkuId(), 
                    inventoryRequest.getWareHouse().getId(), 
                    inventoryRequest.getId()
                ), virtualExecutor);
            CompletableFuture<Inventory> inventoryFuture;
            CompletableFuture<InventoryTransaction> inventoryTranFuture;
            if(isCreate){
                inventoryFuture = CompletableFuture.completedFuture(
                    new Inventory()
                );
                inventoryTranFuture = CompletableFuture.completedFuture(
                    new InventoryTransaction()
                );
            } else {
                inventoryFuture = AsyncUtils.fetchAsyncWThread(
                    () -> inventoryRepository.lockInventoryById(inventoryRequest.getId())
                    .orElseThrow(
                        () -> new ServiceException(
                            EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_ID,
                            "inventory.not.found.id",
                            Map.of("id", inventoryRequest.getId())
                        )
                    )
                    , virtualExecutor);
                
                inventoryTranFuture = AsyncUtils.fetchAsyncWThread(
                    () -> inventoryTransactionRepository.findFirstByProductSkuIdAndReferenceIdOrderByCreatedAtDesc(
                        inventoryRequest.getProductSkuId(),
                        inventoryRequest.getProductId()
                    )
                    .orElseThrow(
                        () -> new ServiceException(
                            EnumError.INVENTORY_INVENTORY_TRANSACTION_ERR_NOT_FOUND_PRODUCT_ID,
                            "inventory.transaction.not.found.productId",
                            Map.of("productSkuId", inventoryRequest.getProductSkuId())
                        )
                    )
                    , virtualExecutor);
            }
            try {
                CompletableFuture.allOf(
                    userFuture, 
                    productValidationFuture, 
                    approvalValidationFuture, 
                    wareHouseFuture, 
                    duplicateCheckFuture, 
                    inventoryFuture,
                    inventoryTranFuture
                ).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }
            WareHouse wareHouse = wareHouseFuture.join();
            Inventory inventory = inventoryFuture.join();
            InventoryTransaction inventoryTransaction = inventoryTranFuture.join();
            
            inventory.setWareHouse(wareHouse);
            inventory.setProductId(inventoryRequest.getProductId());
            inventory.setProductSkuId(inventoryRequest.getProductSkuId());
            inventory.setQuantityAvailable(inventoryRequest.getQuantityAvailable());
            inventory.setQuantityReserved(inventoryRequest.getQuantityReserved());
            inventory.setQuantitySold(inventoryRequest.getQuantitySold());
            inventory.setActivated(true);

            Integer afterAvailableQuantity = inventoryRequest.getQuantityAvailable();
            Integer beforeAvailableQuantity = 0;
            String note;
            if (isCreate) {
                note = String.format("Order products: %s (Available +%d)", inventoryRequest.getProductSkuId(), inventoryRequest.getQuantityAvailable());
            } else {
                note = String.format("Update Inventory data: %s (Available +%d)", inventoryRequest.getProductSkuId(), inventoryRequest.getQuantityAvailable());
                afterAvailableQuantity += inventoryTransaction.getAfterQuantity();
                beforeAvailableQuantity = inventoryTransaction.getAfterQuantity();
            }

            InventoryTransactionTypeEnum tType = isCreate
            ? InventoryTransactionTypeEnum.IMPORT 
            : InventoryTransactionTypeEnum.ADJUSTMENT;

            inventoryTransaction.setActivated(true);
            inventoryTransaction.setNote(note);
            inventoryTransaction.setReferenceId(inventoryRequest.getProductId());
            inventoryTransaction.setReferenceType(InventoryTransactionReferenceTypeEnum.INVENTORY);
            inventoryTransaction.setType(tType);
            inventoryTransaction.setAfterQuantity(afterAvailableQuantity);
            inventoryTransaction.setBeforeQuantity(beforeAvailableQuantity);
            inventoryTransaction.setQuantityChange(Math.abs(inventoryRequest.getQuantityAvailable()));
            inventoryTransaction.setProductSkuId(inventoryRequest.getProductSkuId());
            inventoryTransaction.setEventId(UUID.randomUUID());
            inventoryTransaction.setWareHouse(wareHouse);

            this.inventoryTransactionRepository.save(inventoryTransaction);
            Inventory savedInventory = this.inventoryRepository.save(inventory);
            return inventoryMapper.toDto(savedInventory);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [upSertInventory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkExistedInventory(UUID productId, UUID productSkuId, UUID wareHouseId, UUID excludedId){
        try {
            Optional<Inventory> optional;
            if(excludedId == null){
                optional = this.inventoryRepository.findDuplicateForCreate(productId,productSkuId,wareHouseId);
            } else {
                optional = this.inventoryRepository.findDuplicateForUpdate(productId,productSkuId,wareHouseId,excludedId);
            }
            optional.ifPresent(i -> {
                throw new ServiceException(EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_PRODUCT_PRODUCT_SKU_WARE_HOUSE,"inventory.not.found.productId.productSkuId.wareHouseId", Map.of(
                    "productId",productId,
                    "productSKuId", productSkuId,
                    "wareHouseId", wareHouseId
                ));
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [checkExistedInventory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void validateQuantity(int quantity, UUID skuId) {
        if (quantity < 0) {
            throw new ServiceException(
                    EnumError.INVENTORY_INVENTORY_INVALID_QUANTITY_AVAILABLE,
                    "inventory.quantityAvailable.notFormat",
                    Map.of("sku", skuId)
            );
        }
    }

    private String getCacheKey(UUID id){
        return this.inventoryServiceCacheProperties.createCacheKey(
            this.inventoryServiceCacheProperties.getKeys().getInventoryInfo(),
            id
        );
    }

    private String getLockKey(UUID id){
        return this.inventoryServiceCacheProperties.createLockKey(
            this.inventoryServiceCacheProperties.getKeys().getInventoryInfo(),
            id
        );
    }

    private void updateInventoryCache(InventoryResponse inventoryResponse) {
        try {
            inventoryResponse.setVersion(System.currentTimeMillis());
            
            String cacheKey = this.getCacheKey(inventoryResponse.getId());
            cacheProvider.put(cacheKey, inventoryResponse);
            
            log.info("INVENTORY-SERVICE: Updated cache for inventory ID: {}", inventoryResponse.getId());
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [updateInventoryCache] Error updating cache: {}", e.getMessage(), e);
        }
    }

}
