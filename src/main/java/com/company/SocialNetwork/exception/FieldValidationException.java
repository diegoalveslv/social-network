package com.company.SocialNetwork.exception;

import static java.lang.String.format;

public class FieldValidationException extends RuntimeException {
    private final String fieldName;
    private final String message;

    public FieldValidationException(String fieldName, String message) {
        super(format("Field '%s' is invalid: '%s'", fieldName, message));
        this.fieldName = fieldName;
        this.message = message;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getErrorMessage() {
        return message;
    }
}
