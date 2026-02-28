package com.aegis.agent.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensitiveDataSanitizerTest {

    private final SensitiveDataSanitizer sanitizer = new SensitiveDataSanitizer();

    @Test
    void sanitizeMasksCommonSensitivePatterns() {
        String input = "Contact me at alice@example.com token=abc123 Bearer secret-token and call +1 650 555 1234";

        String redacted = sanitizer.sanitize(input);

        assertFalse(redacted.contains("alice@example.com"));
        assertFalse(redacted.contains("abc123"));
        assertFalse(redacted.contains("secret-token"));
        assertFalse(redacted.contains("650 555 1234"));
        assertTrue(redacted.contains("[redacted-email]"));
        assertTrue(redacted.contains("token=[redacted]"));
        assertTrue(redacted.contains("Bearer [redacted]"));
    }

    @Test
    void pseudonymizeIsStableAndNonEmpty() {
        String one = sanitizer.pseudonymize("user-42");
        String two = sanitizer.pseudonymize("user-42");

        assertEquals(one, two);
        assertTrue(one.startsWith("user-"));
        assertEquals("user-anonymous", sanitizer.pseudonymize("  "));
    }

    @Test
    void sanitizeMapRedactsNestedValues() {
        Map<String, Object> payload = Map.of(
                "query", "email bob@example.com",
                "nested", Map.of("token", "token=xyz"),
                "list", List.of("Bearer hello", "safe")
        );

        Map<String, Object> sanitized = sanitizer.sanitizeMap(payload);

        assertEquals("email [redacted-email]", sanitized.get("query"));
        Map<?, ?> nested = (Map<?, ?>) sanitized.get("nested");
        assertEquals("token=[redacted]", nested.get("token"));
        List<?> list = (List<?>) sanitized.get("list");
        assertEquals("Bearer [redacted]", list.get(0));
    }
}
