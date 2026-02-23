package com.aegis.agent.service;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.IntentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class CloudIntentProvider {

    private static final List<String> ALLOWED_INTENTS = List.of(
            "EnrollmentFailure",
            "GenerateOTP",
            "TokenSyncError",
            "ConfigIssue",
            "ServerUnreachable",
            "PushApprovalTimeout",
            "PasskeyRegistrationFailure",
            "BiometricLockout",
            "TimeDriftFailure",
            "DeviceBindingFailure",
            "Unknown"
    );

    private final AegisProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CloudIntentProvider(AegisProperties properties, RestTemplate externalRestTemplate) {
        this.properties = properties;
        this.restTemplate = externalRestTemplate;
    }

    public IntentResult classify(String query) {
        if (!properties.isCloudIntentEnabled()) {
            return null;
        }
        if (properties.getCloudIntentApiKey() == null || properties.getCloudIntentApiKey().isBlank()) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getCloudIntentApiKey());

            String prompt = "Classify the user query into exactly one IAM support intent. "
                    + "Allowed intents: " + String.join(", ", ALLOWED_INTENTS)
                    + ". Return strict JSON only in this format: "
                    + "{\"intent\":\"<intent>\",\"confidence\":<0_to_1>,\"reason\":\"<short>\"}.";

            Map<String, Object> body = Map.of(
                    "model", properties.getCloudIntentModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", prompt),
                            Map.of("role", "user", "content", query)
                    ),
                    "temperature", 0.0,
                    "response_format", Map.of("type", "json_object")
            );

            Map response = restTemplate.postForObject(
                    properties.getCloudIntentUrl(),
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            return parseResponse(response);
        } catch (RestClientException ex) {
            return null;
        }
    }

    private IntentResult parseResponse(Map response) {
        if (response == null) {
            return null;
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> choice)) {
            return null;
        }
        Object messageObj = choice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            return null;
        }
        String content = extractMessageContent(message.get("content"));
        if (content == null || content.isBlank()) {
            return null;
        }

        try {
            JsonNode node = objectMapper.readTree(content);
            String intent = node.path("intent").asText("Unknown");
            double confidence = node.path("confidence").asDouble(0.0);
            if (!ALLOWED_INTENTS.contains(intent)) {
                intent = "Unknown";
            }
            confidence = Math.max(0.0, Math.min(confidence, 1.0));
            return new IntentResult(intent, confidence);
        } catch (Exception ex) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessageContent(Object contentObj) {
        if (contentObj instanceof String content) {
            return content;
        }
        if (contentObj instanceof List<?> parts) {
            StringBuilder builder = new StringBuilder();
            for (Object part : parts) {
                if (part instanceof Map<?, ?> mapPart) {
                    Object text = mapPart.get("text");
                    if (text instanceof String textValue) {
                        builder.append(textValue);
                    }
                }
            }
            String merged = builder.toString();
            return merged.isBlank() ? null : merged;
        }
        return null;
    }
}
