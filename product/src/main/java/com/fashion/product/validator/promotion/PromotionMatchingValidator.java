package com.fashion.product.validator.promotion;

import org.springframework.stereotype.Component;

import com.fashion.product.common.util.ValidatorFieldUtil;
import com.fashion.product.dto.request.PromotionRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionMatchingValidator implements ConstraintValidator<PromotionMatching, PromotionRequest>{
    ValidatorFieldUtil validatorFieldUtil;
    @Override
    public boolean isValid(PromotionRequest value, ConstraintValidatorContext context) {
        boolean valid = true;
        if (value == null) {
            ValidatorFieldUtil.addViolation(context, "promotion.data.notNull", "PromotionRequest");
            return false;
        }

        // --- BASIC FIELDS ---
        valid &= this.validatorFieldUtil.checkNotBlank(value.getCode(), "promotion.code.notNull", "code", context);
        valid &= this.validatorFieldUtil.checkNotBlank(value.getName(), "promotion.name.notNull", "name", context);
        valid &= this.validatorFieldUtil.checkNotNull(value.getQuantity(), "promotion.quantity.notNull", "quantity", context);
        valid &= this.validatorFieldUtil.checkNotNull(value.getDiscountType(), "promotion.discountType.notNull", "discountType", context);

        // --- DATE VALIDATION ---
        valid &= this.validatorFieldUtil.checkNotNull(value.getStartDate(), "promotion.startDate.notNull", "startDate", context);
        valid &= this.validatorFieldUtil.checkNotNull(value.getEndDate(), "promotion.endDate.notNull", "endDate", context);

        if (value.getStartDate() != null && value.getEndDate() != null &&
            value.getStartDate().isAfter(value.getEndDate())) {
            ValidatorFieldUtil.addViolation(context, "promotion.startDate.before.endDate", "startDate");
            valid = false;
        }
        
        // --- OPTION LOGIC ---
        if (value.getOptionPromotion() != 0 && value.getOptionPromotion() != 1) {
            ValidatorFieldUtil.addViolation(context, "promotion.optionPromotion.notFormat", "optionPromotion");
            valid = false;
        } else if (value.getOptionPromotion() == 0) {
            if (this.validatorFieldUtil.checkNotNull(value.getDiscountPercent(), "promotion.discountPercent.notNull", "discountPercent", context)) {
                if (value.getDiscountPercent() <= 0 || value.getDiscountPercent() > 100) {
                    ValidatorFieldUtil.addViolation(context, "promotion.discountPercent.invalidRange", "discountPercent");
                    valid = false;
                }
            }
            valid &= this.validatorFieldUtil.checkNotNull(value.getDiscountPercent(), "promotion.discountPercent.notNull", "discountPercent", context);
        } else {
            valid &= this.validatorFieldUtil.checkNotNull(value.getMinDiscountAmount(), "promotion.minDiscountAmount.notNull", "minDiscountAmount", context);
            valid &= this.validatorFieldUtil.checkNotNull(value.getMaxDiscountAmount(), "promotion.maxDiscountAmount.notNull", "maxDiscountAmount", context);
        }
        
        return valid;
    }
    
}
