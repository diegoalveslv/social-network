package com.company.SocialNetwork.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeTrimmedValidator implements ConstraintValidator<SizeTrimmed, String> {

    private int min;
    private int max;

    @Override
    public void initialize(SizeTrimmed annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
        validateParameters();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.trim().length() >= min && value.trim().length() <= max;
    }

    private void validateParameters() {
        assert min >= 0 : "Min cannot be negative";
        assert max >= 0 : "Max cannot be negative";
        assert max >= min : "Length cannot be negative";
    }
}
