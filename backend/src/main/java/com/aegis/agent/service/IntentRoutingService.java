package com.aegis.agent.service;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.IntentResolution;
import com.aegis.agent.domain.IntentResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class IntentRoutingService implements IntentService {

    private final DeepPavlovIntentProvider deepPavlovIntentProvider;
    private final CloudIntentProvider cloudIntentProvider;
    private final RuleBasedIntentService fallbackIntentService;
    private final AegisProperties properties;

    public IntentRoutingService(
            DeepPavlovIntentProvider deepPavlovIntentProvider,
            CloudIntentProvider cloudIntentProvider,
            RuleBasedIntentService fallbackIntentService,
            AegisProperties properties
    ) {
        this.deepPavlovIntentProvider = deepPavlovIntentProvider;
        this.cloudIntentProvider = cloudIntentProvider;
        this.fallbackIntentService = fallbackIntentService;
        this.properties = properties;
    }

    @Override
    public IntentResult classify(String query) {
        return classifyResolution(query).getPrimaryIntent();
    }

    @Override
    public IntentResolution classifyResolution(String query, boolean cloudOnly) {
        IntentResult cloudResult = cloudIntentProvider.classify(query);

        if (cloudOnly) {
            if (isKnown(cloudResult)) {
                return IntentResolution.single(cloudResult, "cloud-only retry");
            }
            IntentResult fallback = fallbackIntentService.classify(query);
            return IntentResolution.single(fallback, "cloud-only retry with rule fallback");
        }

        IntentResult deepResult = deepPavlovIntentProvider.classify(query);

        boolean deepKnown = isKnown(deepResult);
        boolean cloudKnown = isKnown(cloudResult);

        if (cloudKnown) {
            if (deepKnown && cloudResult.intent().equalsIgnoreCase(deepResult.intent())) {
                double confidence = Math.max(cloudResult.confidence(), deepResult.confidence());
                return IntentResolution.single(new IntentResult(cloudResult.intent(), confidence), "cloud-primary with DeepPavlov confirmation");
            }
            if (deepKnown) {
                return new IntentResolution(cloudResult, deepResult, "cloud-primary cross-verified by DeepPavlov");
            }
            return IntentResolution.single(cloudResult, "cloud-primary");
        }

        if (deepKnown) {
            return IntentResolution.single(deepResult, "DeepPavlov fallback");
        }

        if (isUsable(cloudResult) && !"Unknown".equalsIgnoreCase(cloudResult.intent())) {
            return IntentResolution.single(cloudResult, "cloud-primary");
        }

        if (isUsable(deepResult) && !"Unknown".equalsIgnoreCase(deepResult.intent())) {
            return IntentResolution.single(deepResult, "DeepPavlov fallback");
        }

        IntentResult fallback = fallbackIntentService.classify(query);
        return IntentResolution.single(fallback, "rule-based fallback");
    }

    private boolean isUsable(IntentResult result) {
        return result != null && result.intent() != null && !result.intent().isBlank();
    }

    private boolean isKnown(IntentResult result) {
        return isUsable(result)
                && result.confidence() >= properties.getConfidenceThreshold()
                && !"Unknown".equalsIgnoreCase(result.intent());
    }
}
