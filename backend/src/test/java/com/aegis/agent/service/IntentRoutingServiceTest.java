package com.aegis.agent.service;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.IntentResolution;
import com.aegis.agent.domain.IntentResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class IntentRoutingServiceTest {

    @Mock
    private DeepPavlovIntentProvider deepPavlovIntentProvider;

    @Mock
    private CloudIntentProvider cloudIntentProvider;

    @Mock
    private RuleBasedIntentService fallbackIntentService;

    @Mock
    private AegisProperties properties;

    private IntentRoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new IntentRoutingService(deepPavlovIntentProvider, cloudIntentProvider, fallbackIntentService, properties);
        given(properties.getConfidenceThreshold()).willReturn(0.8);
    }

    @Test
    void classifyResolutionUsesConsensusWhenBothAgree() {
        given(deepPavlovIntentProvider.classify(anyString())).willReturn(new IntentResult("GenerateOTP", 0.82));
        given(cloudIntentProvider.classify(anyString())).willReturn(new IntentResult("GenerateOTP", 0.91));

        IntentResolution resolution = routingService.classifyResolution("otp not generating");

        assertEquals("GenerateOTP", resolution.getPrimaryIntent().intent());
        assertEquals(0.91, resolution.getPrimaryIntent().confidence());
        assertFalse(resolution.hasSecondaryIntent());
        assertEquals("cloud-primary with DeepPavlov confirmation", resolution.getSourceSummary());
    }

    @Test
    void classifyResolutionBlendsWhenBothKnownAndDifferent() {
        given(deepPavlovIntentProvider.classify(anyString())).willReturn(new IntentResult("GenerateOTP", 0.86));
        given(cloudIntentProvider.classify(anyString())).willReturn(new IntentResult("PushApprovalTimeout", 0.9));

        IntentResolution resolution = routingService.classifyResolution("push challenge timed out");

        assertEquals("PushApprovalTimeout", resolution.getPrimaryIntent().intent());
        assertEquals("GenerateOTP", resolution.getSecondaryIntent().intent());
        assertTrue(resolution.hasSecondaryIntent());
        assertEquals("cloud-primary cross-verified by DeepPavlov", resolution.getSourceSummary());
    }

    @Test
    void classifyResolutionUsesDeepPavlovWhenCloudUnavailable() {
        given(deepPavlovIntentProvider.classify(anyString())).willReturn(new IntentResult("ConfigIssue", 0.84));
        given(cloudIntentProvider.classify(anyString())).willReturn(null);

        IntentResolution resolution = routingService.classifyResolution("config mismatch in app profile");

        assertEquals("ConfigIssue", resolution.getPrimaryIntent().intent());
        assertEquals("DeepPavlov fallback", resolution.getSourceSummary());
    }

    @Test
    void classifyResolutionRetryUsesCloudOnly() {
        given(cloudIntentProvider.classify(anyString())).willReturn(new IntentResult("ServerUnreachable", 0.88));

        IntentResolution resolution = routingService.classifyResolution("retry with prior context", true);

        assertEquals("ServerUnreachable", resolution.getPrimaryIntent().intent());
        assertEquals("cloud-only retry", resolution.getSourceSummary());
    }

    @Test
    void classifyResolutionFallsBackWhenBothUnknown() {
        given(deepPavlovIntentProvider.classify(anyString())).willReturn(new IntentResult("Unknown", 0.1));
        given(cloudIntentProvider.classify(anyString())).willReturn(new IntentResult("Unknown", 0.1));
        given(fallbackIntentService.classify(anyString())).willReturn(new IntentResult("Unknown", 0.2));

        IntentResolution resolution = routingService.classifyResolution("not sure what this is");

        assertEquals("Unknown", resolution.getPrimaryIntent().intent());
        assertEquals("rule-based fallback", resolution.getSourceSummary());
    }
}
