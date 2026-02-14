package com.aegis.agent.integration;

import com.aegis.agent.config.AegisProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OpenSearchClient {

    private final AegisProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public OpenSearchClient(AegisProperties properties) {
        this.properties = properties;
    }

    public void indexEvent(String eventType, Map<String, Object> payload) {
        if (!properties.isOpenSearchEnabled() || properties.getOpenSearchUrl() == null || properties.getOpenSearchUrl().isBlank()) {
            return;
        }

        String url = properties.getOpenSearchUrl() + "/" + properties.getOpenSearchIndex() + "/_doc";

        Map<String, Object> document = new HashMap<>(payload);
        document.put("eventType", eventType);
        document.put("timestamp", Instant.now().toString());

        try {
            restTemplate.postForObject(url, new HttpEntity<>(document, headers()), Map.class);
        } catch (RestClientException ignored) {
            // Non-blocking telemetry path.
        }
    }

    public Map<String, Object> searchByCorrelationId(String correlationId) {
        if (!properties.isOpenSearchEnabled() || properties.getOpenSearchUrl() == null || properties.getOpenSearchUrl().isBlank()) {
            return Map.of();
        }

        String url = properties.getOpenSearchUrl() + "/" + properties.getOpenSearchIndex() + "/_search";
        Map<String, Object> query = Map.of(
                "size", 50,
                "query", Map.of("term", Map.of("correlationId.keyword", correlationId))
        );

        try {
            return restTemplate.postForObject(url, new HttpEntity<>(query, headers()), Map.class);
        } catch (RestClientException ignored) {
            return Map.of();
        }
    }

    public Map<String, Object> searchWithFilters(
            String correlationId,
            String platform,
            String eventType,
            String from,
            String to,
            int size
    ) {
        if (!properties.isOpenSearchEnabled() || properties.getOpenSearchUrl() == null || properties.getOpenSearchUrl().isBlank()) {
            return Map.of();
        }

        String url = properties.getOpenSearchUrl() + "/" + properties.getOpenSearchIndex() + "/_search";
        List<Map<String, Object>> filters = new ArrayList<>();

        if (correlationId != null && !correlationId.isBlank()) {
            filters.add(Map.of("term", Map.of("correlationId.keyword", correlationId)));
        }
        if (platform != null && !platform.isBlank()) {
            filters.add(Map.of("term", Map.of("platform.keyword", platform)));
        }
        if (eventType != null && !eventType.isBlank()) {
            filters.add(Map.of("term", Map.of("eventType.keyword", eventType)));
        }
        if ((from != null && !from.isBlank()) || (to != null && !to.isBlank())) {
            Map<String, Object> range = new HashMap<>();
            if (from != null && !from.isBlank()) {
                range.put("gte", from);
            }
            if (to != null && !to.isBlank()) {
                range.put("lte", to);
            }
            filters.add(Map.of("range", Map.of("timestamp", range)));
        }

        int normalizedSize = Math.max(1, Math.min(size, 200));

        Map<String, Object> query = Map.of(
                "size", normalizedSize,
                "sort", List.of(Map.of("timestamp", Map.of("order", "asc"))),
                "query", Map.of("bool", Map.of("filter", filters.isEmpty() ? List.of(Map.of("match_all", Map.of())) : filters))
        );

        try {
            return restTemplate.postForObject(url, new HttpEntity<>(query, headers()), Map.class);
        } catch (RestClientException ignored) {
            return Map.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> timelineByCorrelationId(String correlationId) {
        Map<String, Object> response = searchByCorrelationId(correlationId);
        return extractEvents(response);
    }

    public List<Map<String, Object>> timelineByFilters(
            String correlationId,
            String platform,
            String eventType,
            String from,
            String to,
            int size
    ) {
        Map<String, Object> response = searchWithFilters(correlationId, platform, eventType, from, to, size);
        return extractEvents(response);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractEvents(Map<String, Object> response) {
        Object hitsObj = response.get("hits");
        if (!(hitsObj instanceof Map<?, ?> hitsMap)) {
            return List.of();
        }

        Object hitListObj = hitsMap.get("hits");
        if (!(hitListObj instanceof List<?> hitList)) {
            return List.of();
        }

        List<Map<String, Object>> events = new ArrayList<>();
        for (Object hit : hitList) {
            if (!(hit instanceof Map<?, ?> hitMap)) {
                continue;
            }
            Object source = hitMap.get("_source");
            if (source instanceof Map<?, ?> srcMap) {
                events.add(new HashMap<>((Map<String, Object>) srcMap));
            }
        }
        return events.stream().filter(Objects::nonNull).toList();
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        if (properties.getOpenSearchUser() != null && !properties.getOpenSearchUser().isBlank()) {
            String raw = properties.getOpenSearchUser() + ":" + (properties.getOpenSearchPassword() == null ? "" : properties.getOpenSearchPassword());
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
        }
        return headers;
    }
}
