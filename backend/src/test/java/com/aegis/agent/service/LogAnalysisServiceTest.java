package com.aegis.agent.service;

import com.aegis.agent.domain.AnalysisResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogAnalysisServiceTest {

    private final LogAnalysisService service = new LogAnalysisService();

    @Test
    void analyzes503Error() {
        AnalysisResult result = service.analyze("2026-02-14 ERROR 503 upstream timeout");
        assertEquals("Service unavailable or upstream outage", result.rootCause());
    }

    @Test
    void analyzesTimeSkew() {
        AnalysisResult result = service.analyze("clock drift detected in otp generation");
        assertEquals("Device time out of sync", result.rootCause());
    }
}
