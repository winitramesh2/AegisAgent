package com.aegis.agent.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class ChatRequest {

    @NotBlank
    private String query;

    @NotBlank
    private String platform;

    @NotBlank
    private String userId;

    @NotNull
    private Map<String, String> deviceMetadata;

    private List<String> chatHistory;

    private boolean troubleshootingFailed;

    private String correlationId;

    private String authProtocol;

    private String challengeId;

    private String priority;

    private List<String> labels;

    private List<String> components;

    private String reporter;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getDeviceMetadata() {
        return deviceMetadata;
    }

    public void setDeviceMetadata(Map<String, String> deviceMetadata) {
        this.deviceMetadata = deviceMetadata;
    }

    public List<String> getChatHistory() {
        return chatHistory;
    }

    public void setChatHistory(List<String> chatHistory) {
        this.chatHistory = chatHistory;
    }

    public boolean isTroubleshootingFailed() {
        return troubleshootingFailed;
    }

    public void setTroubleshootingFailed(boolean troubleshootingFailed) {
        this.troubleshootingFailed = troubleshootingFailed;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getAuthProtocol() {
        return authProtocol;
    }

    public void setAuthProtocol(String authProtocol) {
        this.authProtocol = authProtocol;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }
}
