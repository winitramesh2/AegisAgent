package com.aegis.agent.api.dto;

import java.util.List;

public class ChatResponse {

    private String intent;
    private double confidence;
    private String message;
    private List<String> actions;
    private String escalationTicketId;
    private String status;
    private String correlationId;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getEscalationTicketId() {
        return escalationTicketId;
    }

    public void setEscalationTicketId(String escalationTicketId) {
        this.escalationTicketId = escalationTicketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
