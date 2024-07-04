package com.company.SocialNetwork.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SlugGenerator {
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SLUG_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateSlug() {
        int[] randomIndices = new int[SLUG_LENGTH];
        for (int i = 0; i < SLUG_LENGTH; i++) {
            randomIndices[i] = RANDOM.nextInt(CHAR_SET.length());
        }

        StringBuilder slug = new StringBuilder(SLUG_LENGTH);
        for (int index : randomIndices) {
            slug.append(CHAR_SET.charAt(index));
        }

        return slug.toString();
    }
}