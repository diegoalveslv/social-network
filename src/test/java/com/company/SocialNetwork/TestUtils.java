package com.company.SocialNetwork;

import com.company.SocialNetwork.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    public static List<String> extractMessagesFromBody(String jsonBodyAsString) {
        JsonNode jsonResponse = JsonUtils.readTree(jsonBodyAsString);
        JsonNode messagesJson = jsonResponse.get("messages");
        List<String> messages = new ArrayList<>();
        messagesJson.iterator().forEachRemaining(jsonNode -> messages.add(jsonNode.asText()));
        return messages;
    }
}
