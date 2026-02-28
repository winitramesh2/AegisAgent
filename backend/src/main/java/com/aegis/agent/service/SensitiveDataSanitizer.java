package com.aegis.agent.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SensitiveDataSanitizer {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d[\\d\\s()-]{7,}\\d");
    private static final Pattern JWT_PATTERN = Pattern.compile("\\b[A-Za-z0-9_-]{16,}\\.[A-Za-z0-9_-]{16,}\\.[A-Za-z0-9_-]{16,}\\b");
    private static final Pattern UUID_PATTERN = Pattern.compile("\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\b");
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._\\-]+(?:\\.[A-Za-z0-9._\\-]+){0,2}");
    private static final Pattern KEY_VALUE_SECRET_PATTERN = Pattern.compile(
            "(?i)(api[_-]?key|token|secret|password)\\s*[:=]\\s*[^\\s,;]+"
    );

    public String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String redacted = value;
        redacted = EMAIL_PATTERN.matcher(redacted).replaceAll("[redacted-email]");
        redacted = PHONE_PATTERN.matcher(redacted).replaceAll("[redacted-phone]");
        redacted = JWT_PATTERN.matcher(redacted).replaceAll("[redacted-jwt]");
        redacted = UUID_PATTERN.matcher(redacted).replaceAll("[redacted-uuid]");
        redacted = BEARER_PATTERN.matcher(redacted).replaceAll("Bearer [redacted]");
        redacted = KEY_VALUE_SECRET_PATTERN.matcher(redacted).replaceAll("$1=[redacted]");
        return redacted;
    }

    public String pseudonymize(String value) {
        if (value == null || value.isBlank()) {
            return "user-anonymous";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash);
            return "user-" + hex.substring(0, 12);
        } catch (Exception ignored) {
            return "user-redacted";
        }
    }

    public Map<String, Object> sanitizeMap(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> sanitized = new HashMap<>();
        input.forEach((key, value) -> sanitized.put(key, sanitizeValue(value)));
        return sanitized;
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeValue(Object value) {
        if (value instanceof String str) {
            return sanitize(str);
        }
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> nested = new HashMap<>();
            mapValue.forEach((k, v) -> nested.put(String.valueOf(k), sanitizeValue(v)));
            return nested;
        }
        if (value instanceof List<?> listValue) {
            List<Object> list = new ArrayList<>();
            for (Object item : listValue) {
                list.add(sanitizeValue(item));
            }
            return list;
        }
        return value;
    }
}
