package com.company.SocialNetwork.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static io.micrometer.common.util.StringUtils.isBlank;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private Pattern usernameRegex;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        usernameRegex = Pattern.compile("^[a-zA-Z0-9_-]+$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (isBlank(value) || !usernameRegex.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("invalid format. It should contain only alphanumeric characters, underscore and hyphens").addConstraintViolation();
            return false;
        }

        return false;
    }
}
