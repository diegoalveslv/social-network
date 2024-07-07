package com.company.SocialNetwork.useraccount.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.util.regex.Pattern;

import static io.micrometer.common.util.StringUtils.isBlank;

public class SafeTextFormValidator implements ConstraintValidator<SafeText, String> {

    private Pattern safeTextRegex;
    private String messageTemplate;

    @Override
    public void initialize(SafeText constraintAnnotation) {
        safeTextRegex = SafeTextType.URL_SUPPORTED.REGEX;
        messageTemplate = "invalid format. It should contain only alphanumeric characters, spaces, underscore and hyphens";
        Class<? extends Payload>[] payload = constraintAnnotation.payload();
        for (Class<? extends Payload> payloadClass : payload) {
            if (payloadClass.equals(SafeTextType.URL_SUPPORTED.class)) {
                break;
            }
            if (payloadClass.equals(SafeTextType.USERNAME.class)) {
                safeTextRegex = SafeTextType.USERNAME.REGEX;
                messageTemplate = "invalid format. It should contain only alphanumeric characters, underscore and hyphens";
                break;
            }
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (isBlank(value) || !safeTextRegex.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(messageTemplate).addConstraintViolation();
            return false;
        }

        return true;
    }
}
