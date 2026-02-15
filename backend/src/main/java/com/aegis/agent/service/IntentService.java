package com.aegis.agent.service;

import com.aegis.agent.domain.IntentResolution;
import com.aegis.agent.domain.IntentResult;

public interface IntentService {
    IntentResult classify(String query);

    default IntentResolution classifyResolution(String query) {
        return classifyResolution(query, false);
    }

    default IntentResolution classifyResolution(String query, boolean cloudOnly) {
        return IntentResolution.single(classify(query), cloudOnly ? "cloud-only" : "single intent provider");
    }
}
