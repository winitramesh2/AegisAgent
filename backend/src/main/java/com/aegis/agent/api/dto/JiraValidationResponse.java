package com.aegis.agent.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JiraValidationResponse {

    private boolean jiraConfigured;
    private boolean projectFound;
    private boolean issueTypeFound;
    private boolean priorityFieldAvailable;
    private boolean labelsFieldAvailable;
    private boolean componentsFieldAvailable;
    private boolean reporterFieldAvailable;
    private List<String> missingComponents = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private Map<String, Object> configured;

    public boolean isJiraConfigured() {
        return jiraConfigured;
    }

    public void setJiraConfigured(boolean jiraConfigured) {
        this.jiraConfigured = jiraConfigured;
    }

    public boolean isProjectFound() {
        return projectFound;
    }

    public void setProjectFound(boolean projectFound) {
        this.projectFound = projectFound;
    }

    public boolean isIssueTypeFound() {
        return issueTypeFound;
    }

    public void setIssueTypeFound(boolean issueTypeFound) {
        this.issueTypeFound = issueTypeFound;
    }

    public boolean isPriorityFieldAvailable() {
        return priorityFieldAvailable;
    }

    public void setPriorityFieldAvailable(boolean priorityFieldAvailable) {
        this.priorityFieldAvailable = priorityFieldAvailable;
    }

    public boolean isLabelsFieldAvailable() {
        return labelsFieldAvailable;
    }

    public void setLabelsFieldAvailable(boolean labelsFieldAvailable) {
        this.labelsFieldAvailable = labelsFieldAvailable;
    }

    public boolean isComponentsFieldAvailable() {
        return componentsFieldAvailable;
    }

    public void setComponentsFieldAvailable(boolean componentsFieldAvailable) {
        this.componentsFieldAvailable = componentsFieldAvailable;
    }

    public boolean isReporterFieldAvailable() {
        return reporterFieldAvailable;
    }

    public void setReporterFieldAvailable(boolean reporterFieldAvailable) {
        this.reporterFieldAvailable = reporterFieldAvailable;
    }

    public List<String> getMissingComponents() {
        return missingComponents;
    }

    public void setMissingComponents(List<String> missingComponents) {
        this.missingComponents = missingComponents;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Object> getConfigured() {
        return configured;
    }

    public void setConfigured(Map<String, Object> configured) {
        this.configured = configured;
    }
}
