package com.fashion.product.service.impls;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fashion.product.common.enums.ApprovalMasterEnum;
import com.fashion.product.common.enums.EnumError;
import com.fashion.product.common.response.ApiResponse;
import com.fashion.product.common.util.AsyncUtils;
import com.fashion.product.common.util.PageableUtils;
import com.fashion.product.common.util.SpecificationUtils;
import com.fashion.product.dto.request.ApprovalMasterRequest;
import com.fashion.product.dto.request.search.SearchModel;
import com.fashion.product.dto.request.search.SearchOption;
import com.fashion.product.dto.request.search.SearchRequest;
import com.fashion.product.dto.response.ApprovalMasterResponse;
import com.fashion.product.dto.response.PaginationResponse;
import com.fashion.product.dto.response.PromotionResponse;
import com.fashion.product.dto.response.RoleResponse;
import com.fashion.product.dto.response.UserResponse;
import com.fashion.product.dto.response.RoleResponse.InnerRoleResponse;
import com.fashion.product.dto.response.UserResponse.InnerUserResponse;
import com.fashion.product.entity.ApprovalMaster;
import com.fashion.product.entity.Promotion;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.intergration.IdentityClient;
import com.fashion.product.mapper.ApprovalMasterMapper;
import com.fashion.product.repository.ApprovalMasterRepository;
import com.fashion.product.service.ApprovalMasterService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApprovalMasterServiceImpl implements ApprovalMasterService{
    ApprovalMasterRepository approvalMasterRepository;
    ApprovalMasterMapper approvalMasterMapper;
    IdentityClient identityClient;
    Executor virtualExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalMasterResponse createApprovalMaster(ApprovalMasterRequest request) {
        log.info("PRODUCT-SERVICE: [createApprovalMaster] Start create approval master");
        try {
            ApprovalMaster approvalMaster = this.approvalMasterMapper.toValidated(request);
            return this.saveOrUpdate(null, approvalMaster, request.getVersion());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [createApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public ApprovalMasterResponse updateApprovalMaster(ApprovalMasterRequest request) {
        log.info("PRODUCT-SERVICE: [updateApprovalMaster] Start update approval master");
        try {
            ApprovalMaster approvalMaster = this.approvalMasterMapper.toValidated(request);
            ApprovalMaster existingApprovalMaster = this.approvalMasterRepository.lockApprovalMasterById(approvalMaster.getId());
            return this.saveOrUpdate(existingApprovalMaster, approvalMaster, request.getVersion());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalMasterResponse getApprovalMasterById(UUID id, Long version) {
        try {
            ApprovalMaster approvalMaster = this.approvalMasterRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.PRODUCT_APPROVAL_MASTER_ERR_NOT_FOUND_ID, 
                    "approval.master.not.found.id",
                    Map.of("id", id))
            );
            CompletableFuture<ApiResponse<RoleResponse>> roleFuture = (approvalMaster.getRoleId() != null)
                ? AsyncUtils.fetchAsyncWThread(() -> identityClient.getRoleById(approvalMaster.getRoleId(), version), virtualExecutor)
                : CompletableFuture.completedFuture(null);

            CompletableFuture<ApiResponse<UserResponse>> userFuture = (approvalMaster.getUserId() != null)
                ? AsyncUtils.fetchAsyncWThread(() -> identityClient.getUserById(approvalMaster.getUserId(), version), virtualExecutor)
                : CompletableFuture.completedFuture(null);

            // Đợi cả 2 xong
            try {
                CompletableFuture.allOf(userFuture, roleFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }

            RoleResponse roleRes = roleFuture.join().getData();
            UserResponse userRes = userFuture.join().getData();
            ApprovalMasterResponse response = this.approvalMasterMapper.toDto(approvalMaster);
            response.setRole(
                roleRes != null ?
                InnerRoleResponse.builder()
                .id(roleRes.getId())
                .name(roleRes.getName())
                .slug(roleRes.getSlug())
                .build() :
                null
            );
            response.setUser(
                userRes != null ?
                InnerUserResponse.builder()
                .id(userRes.getId())
                .fullName(userRes.getFullName())
                .email(userRes.getEmail())
                .phoneNumber(userRes.getPhoneNumber())
                .build() :
                null
            );
            return response;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getApprovalMasterById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }  
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<List<ApprovalMasterResponse>> getAllApprovalMaster(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(ApprovalMaster.class);
            Specification<ApprovalMaster> spec = new SpecificationUtils<ApprovalMaster>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<ApprovalMaster> approvalMs = this.approvalMasterRepository.findAll(spec, pageRequest);
            List<ApprovalMasterResponse> aPromotionResponses = this.approvalMasterMapper.toDto(approvalMs.getContent());
            return PageableUtils.<ApprovalMaster, ApprovalMasterResponse>buildPaginationResponse(pageRequest, approvalMs, aPromotionResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [getAllApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteApprovalMasterById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteApprovalMasterById'");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalMaster> findRawAllApprovalMasterByEntityType(String entityType) {
        try {
            return this.approvalMasterRepository.findAllByEntityType(entityType);
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [updateApprovalMaster] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
    
    private ApprovalMasterResponse saveOrUpdate(ApprovalMaster existing, ApprovalMaster approvalMaster, Long version) {
        try {
            checkApprovalMasterExist(approvalMaster, existing == null ? null : existing.getId());

            ApprovalMaster entity = (existing == null) ? new ApprovalMaster() : existing;

            entity.setEntityType(approvalMaster.getEntityType());
            entity.setStatus(approvalMaster.getStatus());
            entity.setStep(approvalMaster.getStep());
            entity.setRequired(approvalMaster.getRequired() == null ? false : approvalMaster.getRequired());
            entity.setActivated(true);
            
            CompletableFuture<ApiResponse<RoleResponse>> roleFuture = (approvalMaster.getRoleId() != null)
                ? AsyncUtils.fetchAsyncWThread(() -> identityClient.getRoleById(approvalMaster.getRoleId(), version), virtualExecutor)
                : CompletableFuture.completedFuture(null);

            CompletableFuture<ApiResponse<UserResponse>> userFuture = (approvalMaster.getUserId() != null)
                ? AsyncUtils.fetchAsyncWThread(() -> identityClient.getUserById(approvalMaster.getUserId(), version), virtualExecutor)
                : CompletableFuture.completedFuture(null);

            try {
                CompletableFuture.allOf(userFuture, roleFuture).join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof ServiceException serviceException) {
                    throw serviceException;
                }
                throw e;
            }
            RoleResponse roleRes = roleFuture.join().getData();
            UserResponse userRes = userFuture.join().getData();

            if (roleRes != null) {
                entity.setRoleId(roleRes.getId());
                entity.setUserId(null);
            } else if (userRes != null) {
                entity.setUserId(userRes.getId());
                if (userRes.getRole() != null) {
                    entity.setRoleId(userRes.getRole().getId());
                }
            } else {
                entity.setUserId(null);
                entity.setRoleId(null);
            }
            // Save
            return approvalMasterMapper.toDto(approvalMasterRepository.save(entity));

        } catch (CompletionException e) {
            log.error("PRODUCT-SERVICE: [saveOrUpdate] Async Error: {}", e.getCause().getMessage());
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [saveOrUpdate] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkApprovalMasterExist(ApprovalMaster req, UUID excludeId) {
        try {
            Optional<ApprovalMaster> optional;
            if(excludeId == null){
                optional = this.approvalMasterRepository.findDuplicateForCreate(
                    req.getEntityType(), 
                    req.getStatus(), 
                    req.getStep()
                );
            } else {
                optional = this.approvalMasterRepository.findDuplicateForUpdate(
                    req.getEntityType(), 
                    req.getStatus(), 
                    req.getStep(),
                    excludeId
                );
            }
            optional.ifPresent((a) -> {
                throw new ServiceException(
                    EnumError.PRODUCT_APPROVAL_MASTER_DATA_EXISTED_ENTITY_TYPE_STATUS_STEP,
                    "approval.master.exist.entityType.status.step",
                    Map.of(
                        "entityType", req.getEntityType(),
                        "status", req.getStatus().name(),
                        "step", req.getStep()
                    )
                );
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [checkApprovalMasterExist] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
