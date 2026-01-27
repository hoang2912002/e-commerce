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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
import com.fashion.inventory.repository.InventoryRepository;
import com.fashion.inventory.repository.InventoryTransactionRepository;
import com.fashion.inventory.repository.WareHouseRepository;
import com.fashion.inventory.security.SecurityUtils;
import com.fashion.inventory.service.InventoryService;
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
    @Transactional(rollbackFor = ServiceException.class)
    public void adjustmentsStockAfterProductApproved(List<ProductApprovedEvent> events, UUID eventId) {
        log.info("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Start import/adjustment inventory");
        try {
            if (events == null || events.isEmpty()) return;
            if(this.inventoryTransactionRepository.existsByEventIdAndReferenceTypeAndReferenceId(eventId,InventoryTransactionReferenceTypeEnum.PRODUCT,events.getFirst().getProductId())){
                log.warn("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Skip duplicated product approved eventId, productId={}", events.getFirst().getProductId(), eventId);
                return;
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
                        (a,b) -> a)
                );
            // Map<UUID, WareHouse> invenWhActive = inventoryMap.values().stream().filter(i -> i.getWareHouse().getStatus().equals(WareHouseStatusEnum.ACTIVE)).collect(Collectors.toMap(Inventory::getId, i -> i.getWareHouse(), (a,b) -> a));
            WareHouse defaultWareHouse = this.wareHouseRepository.findFirstByStatusOrderByCreatedAtDesc(WareHouseStatusEnum.ACTIVE);
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
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [adjustmentsStockAfterProductApproved] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public InventoryResponse createInventory(InventoryRequest inventory) {
        log.info("INVENTORY-SERVICE: [createInventory] Start create inventory");
        try {
            return this.upSertInventory(new Inventory(),inventory);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [createInventory] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public InventoryResponse updateInventory(InventoryRequest inventory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateInventory'");
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        try {
            Inventory inventory = this.inventoryRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.INVENTORY_INVENTORY_ERR_NOT_FOUND_ID,
                    "inventory.not.found.id",
                    Map.of("id", id)
                )
            );

            ApiResponse<ProductResponse> productFuture = this.productClient.getInternalProductByProductId(inventory.getProductId());

            ProductResponse productResponse = productFuture.getData();
            
            InnerProductSkuResponse foundSku = null;
            if (productResponse != null && productResponse.getProductSkus() != null) {
                foundSku = productResponse.getProductSkus().stream()
                    .filter(p -> inventory.getProductSkuId().equals(p.getId()))
                    .findFirst()
                    .orElse(null);
            }

            InventoryResponse inventoryResponse = this.inventoryMapper.toDto(inventory);
            
            if (productResponse != null) {
                inventoryResponse.setProduct(InnerProductResponse.builder()
                .id(productResponse.getId())
                .name(productResponse.getName())
                .build());
            }

            if (foundSku != null) {
                inventoryResponse.setProductSku(InnerProductSkuResponse.builder()
                .id(foundSku.getId())
                .sku(foundSku.getSku())
                .price(foundSku.getPrice())
                .stock(foundSku.getStock())
                .build());
            }

            return inventoryResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("INVENTORY-SERVICE: [getInventoryById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.INVENTORY_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
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
                                .stock(p.getTempStock())
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
    
    private InventoryResponse upSertInventory(Inventory inventory, InventoryRequest inventoryRequest){
        try {
            boolean isCreate = inventory.getId().equals(null);
            UserInsideToken currentUser = SecurityUtils.getCurrentUserId().orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_USER_INVALID_ACCESS_TOKEN, "auth.accessToken.invalid")
            );
            WareHouse wareHouse = this.wareHouseRepository.findById(inventory.getWareHouse().getId()).orElseThrow(
                () -> new ServiceException(EnumError.INVENTORY_WARE_HOUSE_ERR_NOT_FOUND_ID, "ware.house.not.found.id", Map.of("wareHouseId", inventoryRequest.getWarehouse().getId()))
            );
            CompletableFuture<Void> userFuture = (currentUser.getId() != null)
                ? AsyncUtils.fetchAsync(() -> identityClient.validateInternalUserById(currentUser.getId(), true))
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> productFuture = (inventory.getProductId() != null)
                ? AsyncUtils.fetchAsync(() -> productClient.validateInternalProductById(inventory.getProductId(), inventory.getProductSkuId()))
                : CompletableFuture.completedFuture(null);
            
            CompletableFuture<Void> approvalHistoryFuture = (inventory.getProductId() != null)
                ? AsyncUtils.fetchAsync(() -> productClient.validateInternalApprovalHistoryByRequestId(inventory.getProductId()))
                : CompletableFuture.completedFuture(null);
            
            try {
                CompletableFuture.allOf(userFuture, productFuture, approvalHistoryFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }
            
            this.checkExistedInventory(inventoryRequest.getProductId(), inventoryRequest.getProductSkuId(), inventoryRequest.getWarehouse().getId(), inventoryRequest.getId());
            
            return null;
            
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
            if(excludedId.equals(null)){
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

}
