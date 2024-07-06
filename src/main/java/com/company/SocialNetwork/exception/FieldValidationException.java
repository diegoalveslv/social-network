package com.company.SocialNetwork.exception;


public class FieldValidationException extends RuntimeException {
    private final String fieldName;
    private final String message;

    public FieldValidationException(String fieldName, String message) {
        super("Field '%s' is invalid: '%s'".formatted(fieldName, message));
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
