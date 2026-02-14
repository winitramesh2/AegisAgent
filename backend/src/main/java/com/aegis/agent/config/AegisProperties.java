package com.aegis.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "aegis")
public class AegisProperties {

    private double confidenceThreshold = 0.8;
    private String projectKey = "AEGIS";
    private String jiraBaseUrl;
    private String jiraUser;
    private String jiraApiToken;
    private String jiraReporter = "support-bot";
    private String jiraReporterAccountId;
    private String jiraIssueType = "Task";
    private String jiraPriority = "Medium";
    private List<String> jiraLabels = new ArrayList<>(List.of("aegis", "iam", "mfa"));
    private List<String> jiraComponents = new ArrayList<>();
    private String escalationEmailTo;

    private String deeppavlovUrl;
    private boolean deeppavlovEnabled;

    private String openSearchUrl;
    private String openSearchUser;
    private String openSearchPassword;
    private String openSearchIndex = "aegis-events";
    private boolean openSearchEnabled;

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getJiraBaseUrl() {
        return jiraBaseUrl;
    }

    public void setJiraBaseUrl(String jiraBaseUrl) {
        this.jiraBaseUrl = jiraBaseUrl;
    }

    public String getJiraUser() {
        return jiraUser;
    }

    public void setJiraUser(String jiraUser) {
        this.jiraUser = jiraUser;
    }

    public String getJiraApiToken() {
        return jiraApiToken;
    }

    public void setJiraApiToken(String jiraApiToken) {
        this.jiraApiToken = jiraApiToken;
    }

    public String getJiraReporter() {
        return jiraReporter;
    }

    public void setJiraReporter(String jiraReporter) {
        this.jiraReporter = jiraReporter;
    }

    public String getEscalationEmailTo() {
        return escalationEmailTo;
    }

    public void setEscalationEmailTo(String escalationEmailTo) {
        this.escalationEmailTo = escalationEmailTo;
    }

    public String getJiraReporterAccountId() {
        return jiraReporterAccountId;
    }

    public void setJiraReporterAccountId(String jiraReporterAccountId) {
        this.jiraReporterAccountId = jiraReporterAccountId;
    }

    public String getJiraIssueType() {
        return jiraIssueType;
    }

    public void setJiraIssueType(String jiraIssueType) {
        this.jiraIssueType = jiraIssueType;
    }

    public String getJiraPriority() {
        return jiraPriority;
    }

    public void setJiraPriority(String jiraPriority) {
        this.jiraPriority = jiraPriority;
    }

    public List<String> getJiraLabels() {
        return jiraLabels;
    }

    public void setJiraLabels(List<String> jiraLabels) {
        this.jiraLabels = jiraLabels;
    }

    public List<String> getJiraComponents() {
        return jiraComponents;
    }

    public void setJiraComponents(List<String> jiraComponents) {
        this.jiraComponents = jiraComponents;
    }

    public String getDeeppavlovUrl() {
        return deeppavlovUrl;
    }

    public void setDeeppavlovUrl(String deeppavlovUrl) {
        this.deeppavlovUrl = deeppavlovUrl;
    }

    public boolean isDeeppavlovEnabled() {
        return deeppavlovEnabled;
    }

    public void setDeeppavlovEnabled(boolean deeppavlovEnabled) {
        this.deeppavlovEnabled = deeppavlovEnabled;
    }

    public String getOpenSearchUrl() {
        return openSearchUrl;
    }

    public void setOpenSearchUrl(String openSearchUrl) {
        this.openSearchUrl = openSearchUrl;
    }

    public String getOpenSearchUser() {
        return openSearchUser;
    }

    public void setOpenSearchUser(String openSearchUser) {
        this.openSearchUser = openSearchUser;
    }

    public String getOpenSearchPassword() {
        return openSearchPassword;
    }

    public void setOpenSearchPassword(String openSearchPassword) {
        this.openSearchPassword = openSearchPassword;
    }

    public String getOpenSearchIndex() {
        return openSearchIndex;
    }

    public void setOpenSearchIndex(String openSearchIndex) {
        this.openSearchIndex = openSearchIndex;
    }

    public boolean isOpenSearchEnabled() {
        return openSearchEnabled;
    }

    public void setOpenSearchEnabled(boolean openSearchEnabled) {
        this.openSearchEnabled = openSearchEnabled;
    }
}
