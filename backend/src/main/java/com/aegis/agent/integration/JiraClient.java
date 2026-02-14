package com.aegis.agent.integration;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.AnalysisResult;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JiraClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final AegisProperties properties;

    public JiraClient(AegisProperties properties) {
        this.properties = properties;
    }

    public String createTicketAndAttachLog(ChatRequest request, AnalysisResult result, byte[] rawLog, String fileName) {
        if (properties.getJiraBaseUrl() == null || properties.getJiraBaseUrl().isBlank()) {
            return properties.getProjectKey() + "-" + (1000 + (int) (Math.random() * 9000));
        }

        String issueKey = createIssue(request, result);
        if (rawLog != null && rawLog.length > 0) {
            attachLog(issueKey, rawLog, fileName == null ? "client.log" : fileName);
        }
        return issueKey;
    }

    public boolean isHealthy() {
        if (properties.getJiraBaseUrl() == null || properties.getJiraBaseUrl().isBlank()) {
            return false;
        }
        try {
            String url = properties.getJiraBaseUrl() + "/rest/api/2/project/" + properties.getProjectKey();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers(false)), Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public JiraValidationResponse validateFieldMapping() {
        JiraValidationResponse validation = new JiraValidationResponse();
        validation.setConfigured(Map.of(
                "projectKey", properties.getProjectKey(),
                "issueType", properties.getJiraIssueType(),
                "priority", properties.getJiraPriority(),
                "labels", properties.getJiraLabels(),
                "components", properties.getJiraComponents()
        ));

        boolean configured = properties.getJiraBaseUrl() != null
                && !properties.getJiraBaseUrl().isBlank()
                && properties.getJiraUser() != null
                && !properties.getJiraUser().isBlank()
                && properties.getJiraApiToken() != null
                && !properties.getJiraApiToken().isBlank();

        validation.setJiraConfigured(configured);
        if (!configured) {
            validation.getWarnings().add("JIRA credentials or base URL are not fully configured.");
            return validation;
        }

        String projectUrl = properties.getJiraBaseUrl() + "/rest/api/2/project/" + properties.getProjectKey();
        String createmetaUrl = properties.getJiraBaseUrl()
                + "/rest/api/2/issue/createmeta?projectKeys=" + properties.getProjectKey()
                + "&expand=projects.issuetypes.fields";
        String createMetaIssueTypesUrl = properties.getJiraBaseUrl()
                + "/rest/api/2/issue/createmeta/" + properties.getProjectKey() + "/issuetypes";
        String componentsUrl = properties.getJiraBaseUrl()
                + "/rest/api/2/project/" + properties.getProjectKey() + "/components";

        try {
            restTemplate.exchange(projectUrl, HttpMethod.GET, new HttpEntity<>(headers(false)), Map.class);
            validation.setProjectFound(true);

            Map<String, Object> fields = resolveIssueTypeFields(createmetaUrl, createMetaIssueTypesUrl, validation);

            if (fields != null) {
                validation.setPriorityFieldAvailable(fields.containsKey("priority"));
                validation.setLabelsFieldAvailable(fields.containsKey("labels"));
                validation.setComponentsFieldAvailable(fields.containsKey("components"));
                validation.setReporterFieldAvailable(fields.containsKey("reporter"));
            }

            ResponseEntity<List> componentsResp = restTemplate.exchange(componentsUrl, HttpMethod.GET, new HttpEntity<>(headers(false)), List.class);
            List<Map<String, Object>> components = componentsResp.getBody() == null ? List.of() : componentsResp.getBody();
            List<String> available = components.stream()
                    .map(c -> c.get("name"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();

            List<String> missing = properties.getJiraComponents().stream()
                    .filter(c -> !available.contains(c))
                    .toList();
            validation.setMissingComponents(missing);

            if (!missing.isEmpty()) {
                validation.getWarnings().add("Configured components not found: " + String.join(", ", missing));
            }
            if (!validation.isReporterFieldAvailable() && properties.getJiraReporterAccountId() != null && !properties.getJiraReporterAccountId().isBlank()) {
                validation.getWarnings().add("Reporter is configured but reporter field is not available for this project/issue type.");
            }
            if (!validation.isPriorityFieldAvailable()) {
                validation.getWarnings().add("Priority field is not available for this issue type.");
            }
        } catch (RestClientException ex) {
            validation.getWarnings().add("JIRA validation request failed: " + ex.getMessage());
        }

        return validation;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveIssueTypeFields(
            String createmetaUrl,
            String createMetaIssueTypesUrl,
            JiraValidationResponse validation
    ) {
        try {
            ResponseEntity<Map> metaResp = restTemplate.exchange(createmetaUrl, HttpMethod.GET, new HttpEntity<>(headers(false)), Map.class);
            Map body = metaResp.getBody();
            Object projectsObj = body == null ? null : body.get("projects");
            if (!(projectsObj instanceof List<?> projects) || projects.isEmpty()) {
                return null;
            }

            Map firstProject = (Map) projects.get(0);
            Object issueTypesObj = firstProject.get("issuetypes");
            if (!(issueTypesObj instanceof List<?> issueTypes)) {
                return null;
            }

            Map selectedIssueType = findIssueType(issueTypes, properties.getJiraIssueType());
            if (selectedIssueType == null) {
                validation.getWarnings().add("Configured issue type not found: " + properties.getJiraIssueType());
                return null;
            }

            validation.setIssueTypeFound(true);
            Object fieldsObj = selectedIssueType.get("fields");
            return fieldsObj instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        } catch (RestClientException ex) {
            try {
                ResponseEntity<List> issueTypeResp = restTemplate.exchange(createMetaIssueTypesUrl, HttpMethod.GET, new HttpEntity<>(headers(false)), List.class);
                List<Map<String, Object>> issueTypes = issueTypeResp.getBody() == null ? List.of() : issueTypeResp.getBody();
                Map selectedIssueType = findIssueType(issueTypes, properties.getJiraIssueType());
                if (selectedIssueType == null) {
                    validation.getWarnings().add("Configured issue type not found: " + properties.getJiraIssueType());
                    return null;
                }

                validation.setIssueTypeFound(true);
                Object fields = selectedIssueType.get("fields");
                if (fields instanceof Map<?, ?> map) {
                    return (Map<String, Object>) map;
                }

                validation.getWarnings().add("Issue type fields are not exposed by this JIRA endpoint.");
                return Map.of();
            } catch (RestClientException fallbackEx) {
                validation.getWarnings().add("JIRA metadata lookup failed: " + fallbackEx.getMessage());
                return null;
            }
        }
    }

    private Map findIssueType(List<?> issueTypes, String targetName) {
        for (Object it : issueTypes) {
            if (it instanceof Map<?, ?> issueTypeMap) {
                Object name = issueTypeMap.get("name");
                if (targetName.equals(name)) {
                    return (Map) issueTypeMap;
                }
            }
        }
        return null;
    }

    private String createIssue(ChatRequest request, AnalysisResult result) {
        String url = properties.getJiraBaseUrl() + "/rest/api/2/issue";

        Map<String, Object> fields = new HashMap<>();
        fields.put("project", Map.of("key", properties.getProjectKey()));
        fields.put("summary", "[Aegis] " + result.rootCause() + " - " + request.getPlatform());
        fields.put("issuetype", Map.of("name", properties.getJiraIssueType()));
        fields.put("description", buildDescription(request, result));

        String priorityName = request.getPriority() == null || request.getPriority().isBlank()
                ? properties.getJiraPriority()
                : request.getPriority();
        if (priorityName != null && !priorityName.isBlank()) {
            fields.put("priority", Map.of("name", priorityName));
        }

        List<String> labels = request.getLabels() == null || request.getLabels().isEmpty()
                ? properties.getJiraLabels()
                : request.getLabels();
        if (labels != null && !labels.isEmpty()) {
            fields.put("labels", labels);
        }

        List<String> components = request.getComponents() == null || request.getComponents().isEmpty()
                ? properties.getJiraComponents()
                : request.getComponents();
        if (components != null && !components.isEmpty()) {
            fields.put("components", components.stream().map(name -> Map.of("name", name)).collect(Collectors.toList()));
        }

        String reporterAccountId = request.getReporter() == null || request.getReporter().isBlank()
                ? properties.getJiraReporterAccountId()
                : request.getReporter();
        if (reporterAccountId != null && !reporterAccountId.isBlank()) {
            fields.put("reporter", Map.of("id", reporterAccountId));
        }

        Map<String, Object> payload = Map.of("fields", fields);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers(false));
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Object key = response.getBody() == null ? null : response.getBody().get("key");
        return key == null ? properties.getProjectKey() + "-" + UUID.randomUUID().toString().substring(0, 6) : key.toString();
    }

    private void attachLog(String issueKey, byte[] rawLog, String fileName) {
        String url = properties.getJiraBaseUrl() + "/rest/api/2/issue/" + issueKey + "/attachments";

        HttpHeaders headers = headers(true);
        headers.add("X-Atlassian-Token", "no-check");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResourceWithFilename(rawLog, fileName));

        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private HttpHeaders headers(boolean multipart) {
        HttpHeaders headers = new HttpHeaders();

        String authType = properties.getJiraAuthType() == null ? "basic" : properties.getJiraAuthType().trim().toLowerCase();
        if ("bearer".equals(authType)) {
            if (properties.getJiraApiToken() != null && !properties.getJiraApiToken().isBlank()) {
                headers.set("Authorization", "Bearer " + properties.getJiraApiToken());
            }
        } else {
            String user = properties.getJiraUser() == null ? "" : properties.getJiraUser();
            String token = properties.getJiraApiToken() == null ? "" : properties.getJiraApiToken();
            String raw = user + ":" + token;
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
        }

        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        headers.setContentType(multipart ? MediaType.MULTIPART_FORM_DATA : MediaType.APPLICATION_JSON);
        return headers;
    }

    private String buildDescription(ChatRequest request, AnalysisResult result) {
        return "User: " + request.getUserId()
                + "\nPlatform: " + request.getPlatform()
                + "\nQuery: " + request.getQuery()
                + "\nRoot cause: " + result.rootCause()
                + "\nFix action: " + result.fixAction()
                + "\nCorrelation ID: " + request.getCorrelationId();
    }
}
