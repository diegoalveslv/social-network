package com.company.SocialNetwork.useraccount.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    private Pattern passwordRegex;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        passwordRegex = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value) || !passwordRegex.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            List.of("does not meet security requirements"
                    , "should have at least one lowercase letter"
                    , "should have at least one uppercase letter"
                    , "should have at least one special character").forEach(m -> addConstraintViolation(context, m
            ));
            return false;
        }
        return true;
    }

    private void addConstraintViolation(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
