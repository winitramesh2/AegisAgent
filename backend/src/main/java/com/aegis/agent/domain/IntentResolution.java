package com.aegis.agent.domain;

public class IntentResolution {

    private final IntentResult primaryIntent;
    private final IntentResult secondaryIntent;
    private final String sourceSummary;

    public IntentResolution(IntentResult primaryIntent, IntentResult secondaryIntent, String sourceSummary) {
        this.primaryIntent = primaryIntent;
        this.secondaryIntent = secondaryIntent;
        this.sourceSummary = sourceSummary;
    }

    public static IntentResolution single(IntentResult intent, String sourceSummary) {
        return new IntentResolution(intent, null, sourceSummary);
    }

    public IntentResult getPrimaryIntent() {
        return primaryIntent;
    }

    public IntentResult getSecondaryIntent() {
        return secondaryIntent;
    }

    public String getSourceSummary() {
        return sourceSummary;
    }

    public boolean hasSecondaryIntent() {
        return secondaryIntent != null
                && secondaryIntent.intent() != null
                && !secondaryIntent.intent().isBlank()
                && !secondaryIntent.intent().equalsIgnoreCase(primaryIntent.intent())
                && !secondaryIntent.intent().equalsIgnoreCase("Unknown");
    }
}
