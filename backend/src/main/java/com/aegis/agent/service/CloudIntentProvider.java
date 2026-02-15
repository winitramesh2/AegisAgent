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
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CloudIntentProvider(AegisProperties properties) {
        this.properties = properties;
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
        Object contentObj = message.get("content");
        if (!(contentObj instanceof String content) || content.isBlank()) {
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
}
