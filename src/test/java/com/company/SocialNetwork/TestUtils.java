package com.company.SocialNetwork;

public class TestUtils {

    public static String getSlugFromLocation(String location) {
        String[] split = location.split("/");
        return split[split.length - 1];
    }
}
