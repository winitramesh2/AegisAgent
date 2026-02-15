package com.aegis.agent.api;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.domain.AnalysisResult;
import com.aegis.agent.domain.IntentResolution;
import com.aegis.agent.domain.IntentResult;
import com.aegis.agent.integration.JiraClient;
import com.aegis.agent.integration.OpenSearchClient;
import com.aegis.agent.service.DeepPavlovIntentProvider;
import com.aegis.agent.service.EscalationService;
import com.aegis.agent.service.IntentService;
import com.aegis.agent.service.LogAnalysisService;
import com.aegis.agent.service.PlaybookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupportController.class)
class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntentService intentService;

    @MockBean
    private LogAnalysisService logAnalysisService;

    @MockBean
    private PlaybookService playbookService;

    @MockBean
    private EscalationService escalationService;

    @MockBean
    private AegisProperties properties;

    @MockBean
    private OpenSearchClient openSearchClient;

    @MockBean
    private JiraClient jiraClient;

    @MockBean
    private DeepPavlovIntentProvider deepPavlovIntentProvider;

    @Test
    void chatReturnsGuidedResponseWhenConfidenceHigh() throws Exception {
        given(intentService.classifyResolution(anyString(), anyBoolean())).willReturn(IntentResolution.single(new IntentResult("GenerateOTP", 0.92), "cloud-primary with DeepPavlov confirmation"));
        given(playbookService.actionsFor(anyString())).willReturn(List.of("Sync time", "Retry OTP"));

        String payload = """
                {
                  "query": "otp not generating",
                  "platform": "Android",
                  "userId": "user-1",
                  "deviceMetadata": {"model":"Pixel"}
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GUIDED"))
                .andExpect(jsonPath("$.intent").value("GenerateOTP"));
    }

    @Test
    void chatReturnsGuidedResponseWhenIntentKnownEvenIfConfidenceLow() throws Exception {
        given(intentService.classifyResolution(anyString(), anyBoolean())).willReturn(IntentResolution.single(new IntentResult("GenerateOTP", 0.14), "rule-based fallback"));
        given(playbookService.actionsFor(anyString())).willReturn(List.of("Sync time", "Retry OTP"));

        String payload = """
                {
                  "query": "otp not generating",
                  "platform": "Android",
                  "userId": "user-1",
                  "deviceMetadata": {"model":"Pixel"}
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GUIDED"))
                .andExpect(jsonPath("$.intent").value("GenerateOTP"));
    }

    @Test
    void chatDoesNotEscalateAutomaticallyWhenIntentUnknown() throws Exception {
        given(intentService.classifyResolution(anyString(), anyBoolean())).willReturn(IntentResolution.single(new IntentResult("Unknown", 0.2), "rule-based fallback"));
        given(playbookService.actionsFor(anyString())).willReturn(List.of("Upload logs", "Press Retry"));

        String payload = """
                {
                  "query": "something odd",
                  "platform": "Android",
                  "userId": "user-1",
                  "deviceMetadata": {"model":"Pixel"}
                }
                """;

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("GUIDED"))
                .andExpect(jsonPath("$.intent").value("Unknown"));

        verify(escalationService, never()).escalate(any(), any(), any(), any());
    }

    @Test
    void escalateJsonCreatesTicketAndReturnsSlaMessage() throws Exception {
        given(logAnalysisService.analyze(anyString())).willReturn(new AnalysisResult(
                "Push timeout",
                "Check push channel",
                "MEDIUM",
                0.8,
                List.of("push timeout")
        ));
        given(escalationService.escalate(any(), any(), any(), any())).willReturn("TAC-999");

        String payload = """
                {
                  "query": "push still failing",
                  "platform": "Android",
                  "userId": "user-1",
                  "deviceMetadata": {"model":"Pixel"}
                }
                """;

        mockMvc.perform(post("/api/escalate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ESCALATED"))
                .andExpect(jsonPath("$.escalationTicketId").value("TAC-999"))
                .andExpect(jsonPath("$.message").value("Issue escalated successfully. Please wait for 3 working days and the support team will contact you."));
    }

    @Test
    void incidentTimelineReturnsEvents() throws Exception {
        given(openSearchClient.timelineByCorrelationId(eq("corr-123"))).willReturn(List.of(
                Map.of("eventType", "CHAT_GUIDED", "correlationId", "corr-123"),
                Map.of("eventType", "LOG_ANALYSIS", "correlationId", "corr-123")
        ));

        mockMvc.perform(get("/api/incidents/corr-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.events[0].eventType").value("CHAT_GUIDED"));
    }

    @Test
    void incidentTimelineByFiltersReturnsEvents() throws Exception {
        given(openSearchClient.timelineByFilters(eq("corr-999"), eq("Android"), eq("CHAT_ESCALATED"), eq("2026-02-14T10:00:00Z"), eq("2026-02-14T12:00:00Z"), eq(20)))
                .willReturn(List.of(Map.of("eventType", "CHAT_ESCALATED", "platform", "Android")));

        mockMvc.perform(get("/api/incidents")
                        .queryParam("correlationId", "corr-999")
                        .queryParam("platform", "Android")
                        .queryParam("eventType", "CHAT_ESCALATED")
                        .queryParam("from", "2026-02-14T10:00:00Z")
                        .queryParam("to", "2026-02-14T12:00:00Z")
                        .queryParam("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.events[0].eventType").value("CHAT_ESCALATED"));
    }

    @Test
    void jiraValidationEndpointReturnsStatus() throws Exception {
        JiraValidationResponse response = new JiraValidationResponse();
        response.setJiraConfigured(true);
        response.setProjectFound(true);
        response.setIssueTypeFound(true);
        given(jiraClient.validateFieldMapping()).willReturn(response);

        mockMvc.perform(get("/api/admin/jira/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jiraConfigured").value(true))
                .andExpect(jsonPath("$.projectFound").value(true))
                .andExpect(jsonPath("$.issueTypeFound").value(true));
    }

    @Test
    void componentStatusReturnsServiceHealthStates() throws Exception {
        given(deepPavlovIntentProvider.isHealthy()).willReturn(true);
        given(openSearchClient.isHealthy()).willReturn(true);
        given(jiraClient.isHealthy()).willReturn(false);

        mockMvc.perform(get("/api/status/components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.backend.status").value("UP"))
                .andExpect(jsonPath("$.components.deeppavlov.status").value("UP"))
                .andExpect(jsonPath("$.components.opensearch.status").value("UP"))
                .andExpect(jsonPath("$.components.jira.status").value("DOWN"));
    }
}
