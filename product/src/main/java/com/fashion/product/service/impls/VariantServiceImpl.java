package com.fashion.product.service.impls;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fashion.product.common.enums.EnumError;
import com.fashion.product.exception.ServiceException;
import com.fashion.product.repository.VariantRepository;
import com.fashion.product.service.VariantService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class VariantServiceImpl implements VariantService{
    VariantRepository variantRepository;

    @Override
    @Transactional(rollbackFor = ServiceException.class, timeout = 30)
    public void deleteAllByProductId(UUID uuid) {
        try {
            this.variantRepository.deleteAllByProductId(uuid);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("PRODUCT-SERVICE: [deleteAllByProductId] Error: {}", e.getMessage(), e);
            throw new ServiceException(EnumError.PRODUCT_INTERNAL_ERROR_CALL_API, "server.error.internal");
        }
    }


}
