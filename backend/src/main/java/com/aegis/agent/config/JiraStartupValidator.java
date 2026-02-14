package com.aegis.agent.config;

import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.integration.JiraClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JiraStartupValidator {

    private static final Logger log = LoggerFactory.getLogger(JiraStartupValidator.class);

    private final AegisProperties properties;
    private final JiraClient jiraClient;

    public JiraStartupValidator(AegisProperties properties, JiraClient jiraClient) {
        this.properties = properties;
        this.jiraClient = jiraClient;
    }

    @PostConstruct
    public void validateAtStartup() {
        if (!properties.isJiraValidationOnStartup()) {
            return;
        }

        JiraValidationResponse validation = jiraClient.validateFieldMapping();
        boolean isValid = validation.isJiraConfigured()
                && validation.isProjectFound()
                && validation.isIssueTypeFound();

        if (isValid) {
            log.info("JIRA startup validation passed for project {}", properties.getProjectKey());
            return;
        }

        String message = "JIRA startup validation failed: " + String.join(" | ", validation.getWarnings());
        if (properties.isFailOnJiraValidation()) {
            throw new IllegalStateException(message);
        }
        log.warn(message);
    }
}
