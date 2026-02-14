package com.aegis.agent.domain;

import java.util.List;

public record AnalysisResult(
        String rootCause,
        String fixAction,
        String severity,
        double confidence,
        List<String> matchedSignals
) {
}
