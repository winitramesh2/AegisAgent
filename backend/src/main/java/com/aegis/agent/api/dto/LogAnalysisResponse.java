package com.aegis.agent.api.dto;

import java.util.List;

public class LogAnalysisResponse {

    private String rootCause;
    private String fixAction;
    private String severity;
    private double confidence;
    private List<String> matchedSignals;
    private String correlationId;

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getFixAction() {
        return fixAction;
    }

    public void setFixAction(String fixAction) {
        this.fixAction = fixAction;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<String> getMatchedSignals() {
        return matchedSignals;
    }

    public void setMatchedSignals(List<String> matchedSignals) {
        this.matchedSignals = matchedSignals;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
