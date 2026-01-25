package com.fashion.order.service.impls;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.StaleStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.order.common.enums.EnumError;
import com.fashion.order.common.util.FormatTime;
import com.fashion.order.common.util.PageableUtils;
import com.fashion.order.common.util.SpecificationUtils;
import com.fashion.order.dto.request.CouponRequest;
import com.fashion.order.dto.request.search.SearchModel;
import com.fashion.order.dto.request.search.SearchOption;
import com.fashion.order.dto.request.search.SearchRequest;
import com.fashion.order.dto.response.CouponResponse;
import com.fashion.order.dto.response.PaginationResponse;
import com.fashion.order.entity.Coupon;
import com.fashion.order.exception.ServiceException;
import com.fashion.order.mapper.CouponMapper;
import com.fashion.order.repository.CouponRepository;
import com.fashion.order.service.CouponService;

import jakarta.persistence.OptimisticLockException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CouponServiceImpl implements CouponService{
    CouponMapper couponMapper;
    CouponRepository couponRepository;

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public CouponResponse createCoupon(CouponRequest request) {
        log.info("ORDER-SERVICE: [createCoupon] start create coupon ....");
        try {
            return upSert(request, new Coupon());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [createCoupon] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public CouponResponse updateCoupon(CouponRequest request) {
        log.info("ORDER-SERVICE: [updateCoupon] start update coupon ....");
        try {
            Coupon coupon = this.couponRepository.findById(request.getId()).orElseThrow(
                () ->new ServiceException(EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, "coupon.not.found.id",Map.of("id", request.getId())
            ));
            if(!coupon.getVersion().equals(request.getVersion())){
                throw new ServiceException(EnumError.ORDER_COUPON_INVALID_SIMILAR_VERSION, "coupon.version.notSimilar.currentVersion");
            }
            return upSert(request, coupon); 
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [createCoupon] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCouponById(UUID id) {
        try {
            Coupon coupon = this.couponRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, 
                    "coupon.not.found.id",
                    Map.of("id", id
                ))
            );
            return this.couponMapper.toDto(coupon);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [getCouponById] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public PaginationResponse<List<CouponResponse>> getAllCoupons(SearchRequest request) {
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
            List<String> fields = SpecificationUtils.getFieldsSearch(Coupon.class);
            Specification<Coupon> spec = new SpecificationUtils<Coupon>()
                .equal("activated", searchModel.getActivated())
                .likeAnyFieldIgnoreCase(searchModel.getQ(), fields)
                .build();

            Page<Coupon> coupons = this.couponRepository.findAll(spec, pageRequest);
            List<CouponResponse> couponResponses = this.couponMapper.toDto(coupons.getContent());
            return PageableUtils.<Coupon, CouponResponse>buildPaginationResponse(pageRequest, coupons, couponResponses);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [getAllCoupons] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void deleteCouponById(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteCouponById'");
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon validateCouponOrder(UUID id) {
        try {
            Coupon coupon = this.couponRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, 
                    "coupon.not.found.id",
                    Map.of("id", id
                ))
            );
            if(coupon.getEndDate().isBefore(LocalDateTime.now())){
                throw new ServiceException(
                    EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, 
                    "coupon.endDate.before.currentDate",
                    Map.of("endDate", FormatTime.StringDateLocalDateTime(coupon.getEndDate())));
            }
            if(coupon.getStock() <= 0){
                throw new ServiceException(
                    EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, 
                    "coupon.stock.out",
                    Map.of("stock", coupon.getStock()));
            }
            return coupon;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [validateCouponOrder] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    @Transactional(rollbackFor = ServiceException.class)
    public void decreaseStock(UUID id) {
        try {
            Coupon coupon = this.couponRepository.findById(id).orElseThrow(
                () -> new ServiceException(
                    EnumError.ORDER_COUPON_ERR_NOT_FOUND_ID, 
                    "coupon.not.found.id",
                    Map.of("id", id
                ))
            );
            if(coupon.getStock() <= 0){
                throw new ServiceException(
                    EnumError.ORDER_COUPON_DATA_OUT_STOCK, 
                    "coupon.stock.out"
                );
            }
            coupon.setStock(coupon.getStock() - 1);
            this.couponRepository.flush();
        } catch (ObjectOptimisticLockingFailureException | StaleStateException e){
            throw new ServiceException(EnumError.ORDER_COUPON_INVALID_SIMILAR_VERSION, "coupon.version.notSimilar.currentVersion");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [decreaseStock] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    @Override
    public void increaseStock(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'increaseStock'");
    }

    private CouponResponse upSert(CouponRequest request, Coupon coupon){
        try {
            this.checkExistedCoupon(request.getCode(), request.getId());
            if(request.getId() != null){
                this.couponMapper.toUpdate(coupon, request);
            } else {
                coupon = this.couponMapper.toValidated(request);
            }
            coupon.setActivated(true);
            Coupon saveCoupon = this.couponRepository.saveAndFlush(coupon);
            return couponMapper.toDto(saveCoupon);
        } catch (OptimisticLockingFailureException e) {
            // Bắt lỗi xung đột version thực tế lúc ghi xuống DB
            throw new ServiceException(EnumError.ORDER_COUPON_INVALID_SIMILAR_VERSION, "coupon.version.notSimilar.currentVersion");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [upSert] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }

    private void checkExistedCoupon(String code, UUID excluded){
        try {
            Optional<Coupon> optional;
            if(excluded == null){
                optional = this.couponRepository.findByCode(code);
            } else {
                optional = this.couponRepository.findByCodeAndIdNot(code, excluded);
            }
            optional.ifPresent(o -> {
                throw new ServiceException(
                    EnumError.ORDER_COUPON_DATA_EXISTED_CODE, 
                    "coupon.exist.code",
                    Map.of("code", code
                ));
            });
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORDER-SERVICE: [checkExistedCoupon] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.ORDER_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }
}
