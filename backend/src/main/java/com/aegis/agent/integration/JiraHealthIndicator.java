package com.aegis.agent.integration;

import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.config.AegisProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class JiraHealthIndicator implements HealthIndicator {

    private final JiraClient jiraClient;
    private final AegisProperties properties;

    public JiraHealthIndicator(JiraClient jiraClient, AegisProperties properties) {
        this.jiraClient = jiraClient;
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (properties.getJiraBaseUrl() == null || properties.getJiraBaseUrl().isBlank()) {
            return Health.unknown().withDetail("jira", "Not configured").build();
        }

        JiraValidationResponse validation = jiraClient.validateFieldMapping();
        boolean up = validation.isJiraConfigured() && validation.isProjectFound() && validation.isIssueTypeFound();

        Health.Builder builder = up ? Health.up() : Health.down();
        return builder
                .withDetail("projectKey", properties.getProjectKey())
                .withDetail("issueType", properties.getJiraIssueType())
                .withDetail("warnings", validation.getWarnings())
                .build();
    }
}
