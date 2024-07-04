package com.company.SocialNetwork.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static io.micrometer.common.util.StringUtils.isBlank;

public class SafeTextFormValidator implements ConstraintValidator<SafeText, String> {

    private Pattern safeTextRegex;

    @Override
    public void initialize(SafeText constraintAnnotation) {
        safeTextRegex = Pattern.compile("^[ \\w_-]*$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (isBlank(value) || !safeTextRegex.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens").addConstraintViolation();
            return false;
        }

        return true;
    }
}
