package com.aegis.agent.api;

import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.domain.IntentResult;
import com.aegis.agent.integration.JiraClient;
import com.aegis.agent.integration.OpenSearchClient;
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

    @Test
    void chatReturnsGuidedResponseWhenConfidenceHigh() throws Exception {
        given(intentService.classify(anyString())).willReturn(new IntentResult("GenerateOTP", 0.92));
        given(playbookService.actionsFor(anyString())).willReturn(List.of("Sync time", "Retry OTP"));
        given(properties.getConfidenceThreshold()).willReturn(0.8);

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
}
