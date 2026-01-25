package com.fashion.order.validator.coupon;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.fashion.order.common.enums.CouponEnum;
import com.fashion.order.common.util.ValidatorFieldUtil;
import com.fashion.order.dto.request.CouponRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class CouponMatchingValidator implements ConstraintValidator<CouponMatching, CouponRequest>{
    ValidatorFieldUtil validatorField;

    @Override
    public boolean isValid(CouponRequest value, ConstraintValidatorContext context) {
        boolean valid = true;
        if (value == null) {
            ValidatorFieldUtil.addViolation(context, "coupon.data.notNull", "CouponRequest");
            return false;
        }
        if(value.getStartDate() != null && value.getEndDate() != null && 
            value.getStartDate().isAfter(value.getEndDate())
        ){
            ValidatorFieldUtil.addViolation(context, "coupon.startDate.before.endDate", "startDate");
            valid = false;
        }
        
        // --- TYPE PERCENT ---
        if(value.getType().equals(CouponEnum.PERCENT) && value.getCouponAmount().compareTo(new BigDecimal("100")) > 0){
            ValidatorFieldUtil.addViolation(context, "coupon.type.percent.amount", "type");
            valid = false;
        }
        return valid;
    }
    
}
