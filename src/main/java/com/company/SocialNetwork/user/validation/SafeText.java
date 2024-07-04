package com.company.SocialNetwork.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTextFormValidator.class)
public @interface SafeText {
    String message() default "Invalid text format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
