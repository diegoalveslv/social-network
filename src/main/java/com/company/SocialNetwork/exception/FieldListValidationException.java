package com.company.SocialNetwork.exception;


import java.util.HashMap;
import java.util.Map;

public class FieldListValidationException extends RuntimeException {
    private final Map<String, String> messageToFieldName = new HashMap<>();

    public FieldListValidationException(Map<String, String> messageToFieldName) {
        super("multiple fields failed validation");
        this.messageToFieldName.putAll(messageToFieldName);
    }

    public Map<String, String> getMessageToFieldName() {
        return messageToFieldName;
    }
}
