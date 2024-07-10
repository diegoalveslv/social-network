package com.company.SocialNetwork.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;

public class JsonUtils {

    private static final ObjectMapper defaultMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .build();

    private JsonUtils() {
    }

    public static String asJsonString(Object obj) throws RuntimeJsonProcessingJson {
        try {
            return defaultMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonProcessingJson(e);
        }
    }

    public static JsonNode readTree(String contentAsString) {
        try {
            return defaultMapper.readTree(contentAsString);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonProcessingJson(e);
        }
    }

    public static <T> T readValue(String contentAsString, Class<T> type) {
        try {
            return defaultMapper.readValue(contentAsString, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonProcessingJson(e);
        }
    }

    public static <T> T readValue(String contentAsString, TypeReference<T> type) {
        try {
            return defaultMapper.readValue(contentAsString, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeJsonProcessingJson(e);
        }
    }

    public static <T> T readFile(File src, Class<T> type) {
        try {
            return defaultMapper.readValue(src, type);
        } catch (IOException e) {
            throw new RuntimeJsonProcessingJson(new RuntimeException(e));
        }
    }

    public static class RuntimeJsonProcessingJson extends RuntimeException {
        public RuntimeJsonProcessingJson(RuntimeException e) {
            super(e);
        }

        public RuntimeJsonProcessingJson(JsonProcessingException e) {
            super(e);
        }
    }
}