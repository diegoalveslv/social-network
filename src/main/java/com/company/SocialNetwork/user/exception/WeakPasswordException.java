package com.company.SocialNetwork.user.exception;

import com.company.SocialNetwork.exception.UnprocessableEntityException;

import java.util.List;

public class WeakPasswordException extends UnprocessableEntityException {

    private final List<String> messages;

    public WeakPasswordException(List<String> messages) {
        super();
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
