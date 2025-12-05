package com.hivemind.datastream.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

public class EventProcessor implements MapFunction<String, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String map(String value) throws Exception {
        try {
            JsonNode node = objectMapper.readTree(value);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : "UNKNOWN";
            String deviceId = node.has("deviceId") ? node.get("deviceId").asText() : "UNKNOWN";
            String severity = node.has("severity") ? node.get("severity").asText() : "UNKNOWN";
            String username = node.has("username") ? node.get("username").asText() : "N/A";
            String authStatus = node.has("authenticationStatus") ? node.get("authenticationStatus").asText() : "N/A";

            // Simple processing: Log high severity events
            if ("HIGH".equals(severity) || "CRITICAL".equals(severity)) {
                return String.format(
                        "⚠️ ALERT: High severity event detected! [Type: %s, Device: %s, Severity: %s, User: %s, Auth: %s]",
                        eventType, deviceId, severity, username, authStatus);
            }

            return String.format("ℹ️ Processed event: [Type: %s, Device: %s, User: %s]", eventType, deviceId, username);
        } catch (Exception e) {
            return "❌ Error processing event: " + e.getMessage();
        }
    }
}
