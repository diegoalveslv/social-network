package com.company.SocialNetwork.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpStatusCodes {

    public static final String OK = "200";
    public static final String CREATED = "201";
    public static final String ACCEPTED = "202";
    public static final String NO_CONTENT = "204";

    public static final String FOUND_REDIRECT = "302";

    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String PRECONDITION_FAILED = "412";
    public static final String UNPROCESSABLE_ENTITY = "422";

    public static final String INTERNAL_SERVER_ERROR = "500";

}
