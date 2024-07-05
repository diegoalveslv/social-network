package com.company.SocialNetwork.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class NotBlankTrimmedValidator implements ConstraintValidator<NotBlankTrimmed, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && isNotBlank(value.trim());
    }
}
