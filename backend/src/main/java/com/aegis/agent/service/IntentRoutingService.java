package com.aegis.agent.service;

import com.aegis.agent.domain.IntentResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class IntentRoutingService implements IntentService {

    private final DeepPavlovIntentProvider deepPavlovIntentProvider;
    private final RuleBasedIntentService fallbackIntentService;

    public IntentRoutingService(DeepPavlovIntentProvider deepPavlovIntentProvider, RuleBasedIntentService fallbackIntentService) {
        this.deepPavlovIntentProvider = deepPavlovIntentProvider;
        this.fallbackIntentService = fallbackIntentService;
    }

    @Override
    public IntentResult classify(String query) {
        IntentResult external = deepPavlovIntentProvider.classify(query);
        if (external != null && external.intent() != null && !external.intent().isBlank()) {
            return external;
        }
        return fallbackIntentService.classify(query);
    }
}
