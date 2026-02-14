package com.aegis.agent.service;

import com.aegis.agent.domain.IntentResult;

public interface IntentService {
    IntentResult classify(String query);
}
