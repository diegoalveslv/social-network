package com.company.SocialNetwork.useraccount.validation;

import jakarta.validation.Payload;

import java.util.regex.Pattern;

public class SafeTextType {
    public static class URL_SUPPORTED implements Payload {
        static Pattern REGEX = Pattern.compile("^[ \\w_-]*$"); //accepts space, alphanumeric, underscore and hyphen
    }

    public static class USERNAME implements Payload {
        static Pattern REGEX = Pattern.compile("^[\\w_-]*$"); //accepts alphanumeric, underscore and hyphen
    }
}
