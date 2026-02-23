package com.aegis.agent.service;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.IntentResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class DeepPavlovIntentProvider {

    private final AegisProperties properties;
    private final RestTemplate restTemplate;

    public DeepPavlovIntentProvider(AegisProperties properties, RestTemplate externalRestTemplate) {
        this.properties = properties;
        this.restTemplate = externalRestTemplate;
    }

    public IntentResult classify(String query) {
        if (!properties.isDeeppavlovEnabled() || properties.getDeeppavlovUrl() == null || properties.getDeeppavlovUrl().isBlank()) {
            return null;
        }

        Map<String, Object> payload = Map.of("query", query, "texts", List.of(query));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            Map response = restTemplate.postForObject(properties.getDeeppavlovUrl(), new HttpEntity<>(payload, headers), Map.class);
            return parseResponse(response);
        } catch (RestClientException ex) {
            return null;
        }
    }

    public boolean isHealthy() {
        if (!properties.isDeeppavlovEnabled() || properties.getDeeppavlovUrl() == null || properties.getDeeppavlovUrl().isBlank()) {
            return false;
        }
        String inferUrl = properties.getDeeppavlovUrl();
        String healthUrl = inferUrl.endsWith("/infer") ? inferUrl.substring(0, inferUrl.length() - 6) + "/health" : inferUrl + "/health";
        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(healthUrl, String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private IntentResult parseResponse(Map response) {
        if (response == null) {
            return null;
        }

        Object intent = response.get("intent");
        Object confidence = response.get("confidence");
        if (intent instanceof String && confidence instanceof Number) {
            return new IntentResult((String) intent, ((Number) confidence).doubleValue());
        }

        Object predictions = response.get("predictions");
        if (predictions instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            Object label = map.get("label");
            Object score = map.get("score");
            if (label instanceof String && score instanceof Number) {
                return new IntentResult((String) label, ((Number) score).doubleValue());
            }
        }

        return null;
    }
}
