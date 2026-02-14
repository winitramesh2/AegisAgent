package com.aegis.agent.api;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.api.dto.ChatResponse;
import com.aegis.agent.api.dto.LogAnalysisResponse;
import com.aegis.agent.api.dto.IncidentTimelineResponse;
import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.AnalysisResult;
import com.aegis.agent.domain.IntentResult;
import com.aegis.agent.integration.JiraClient;
import com.aegis.agent.integration.OpenSearchClient;
import com.aegis.agent.service.EscalationService;
import com.aegis.agent.service.IntentService;
import com.aegis.agent.service.LogAnalysisService;
import com.aegis.agent.service.PlaybookService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SupportController {

    private final IntentService intentService;
    private final LogAnalysisService logAnalysisService;
    private final PlaybookService playbookService;
    private final EscalationService escalationService;
    private final AegisProperties properties;
    private final OpenSearchClient openSearchClient;
    private final JiraClient jiraClient;

    public SupportController(
            IntentService intentService,
            LogAnalysisService logAnalysisService,
            PlaybookService playbookService,
            EscalationService escalationService,
            AegisProperties properties,
            OpenSearchClient openSearchClient,
            JiraClient jiraClient
    ) {
        this.intentService = intentService;
        this.logAnalysisService = logAnalysisService;
        this.playbookService = playbookService;
        this.escalationService = escalationService;
        this.properties = properties;
        this.openSearchClient = openSearchClient;
        this.jiraClient = jiraClient;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String correlationId = request.getCorrelationId() == null || request.getCorrelationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.getCorrelationId();
        request.setCorrelationId(correlationId);

        IntentResult intent = intentService.classify(request.getQuery());

        ChatResponse response = new ChatResponse();
        response.setCorrelationId(correlationId);
        response.setIntent(intent.intent());
        response.setConfidence(intent.confidence());

        if (request.isTroubleshootingFailed() || intent.confidence() < properties.getConfidenceThreshold()) {
            AnalysisResult analysis = logAnalysisService.analyze(request.getQuery());
            String ticket = escalationService.escalate(request, analysis, null, null);
            response.setStatus("ESCALATED");
            response.setEscalationTicketId(ticket);
            response.setMessage("Issue escalated with full context.");
            response.setActions(List.of("Track ticket: " + ticket));
            indexChatEvent("CHAT_ESCALATED", request, intent, analysis, ticket);
            return response;
        }

        response.setStatus("GUIDED");
        response.setMessage("Here are first-aid troubleshooting steps.");
        response.setActions(playbookService.actionsFor(intent.intent()));
        indexChatEvent("CHAT_GUIDED", request, intent, null, null);
        return response;
    }

    @PostMapping(value = "/analyze-logs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LogAnalysisResponse analyzeLogs(
            @RequestPart("logFile") MultipartFile logFile,
            @RequestPart(value = "correlationId", required = false) String correlationId
    ) throws IOException {
        String rawLog = new String(logFile.getBytes());
        AnalysisResult result = logAnalysisService.analyze(rawLog);

        LogAnalysisResponse response = new LogAnalysisResponse();
        response.setRootCause(result.rootCause());
        response.setFixAction(result.fixAction());
        response.setSeverity(result.severity());
        response.setConfidence(result.confidence());
        response.setMatchedSignals(result.matchedSignals());
        response.setCorrelationId(correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId);
        Map<String, Object> event = new HashMap<>();
        event.put("correlationId", response.getCorrelationId());
        event.put("rootCause", response.getRootCause());
        event.put("fixAction", response.getFixAction());
        event.put("severity", response.getSeverity());
        event.put("confidence", response.getConfidence());
        event.put("matchedSignals", response.getMatchedSignals());
        openSearchClient.indexEvent("LOG_ANALYSIS", event);
        return response;
    }

    @PostMapping(value = "/escalate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse escalate(
            @RequestPart("request") @Valid ChatRequest request,
            @RequestPart(value = "logFile", required = false) MultipartFile logFile
    ) throws IOException {
        String correlationId = request.getCorrelationId() == null || request.getCorrelationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.getCorrelationId();
        request.setCorrelationId(correlationId);

        byte[] logBytes = logFile == null ? null : logFile.getBytes();
        String rawLog = logFile == null ? request.getQuery() : new String(logBytes);
        AnalysisResult analysis = logAnalysisService.analyze(rawLog);

        String ticket = escalationService.escalate(request, analysis, logBytes, logFile == null ? null : logFile.getOriginalFilename());

        ChatResponse response = new ChatResponse();
        response.setStatus("ESCALATED");
        response.setIntent("Escalation");
        response.setConfidence(1.0);
        response.setCorrelationId(correlationId);
        response.setEscalationTicketId(ticket);
        response.setMessage("Escalation created successfully.");
        response.setActions(List.of("Ticket ID: " + ticket));

        indexChatEvent("MANUAL_ESCALATION", request, new IntentResult("Escalation", 1.0), analysis, ticket);
        return response;
    }

    @GetMapping("/incidents/{correlationId}")
    public IncidentTimelineResponse incidentTimeline(@PathVariable String correlationId) {
        List<Map<String, Object>> events = openSearchClient.timelineByCorrelationId(correlationId);
        IncidentTimelineResponse response = new IncidentTimelineResponse();
        response.setCorrelationId(correlationId);
        response.setEvents(events);
        response.setTotal(events.size());
        return response;
    }

    @GetMapping("/admin/jira/validate")
    public JiraValidationResponse validateJiraMapping() {
        return jiraClient.validateFieldMapping();
    }

    private void indexChatEvent(String eventType, ChatRequest request, IntentResult intent, AnalysisResult analysis, String ticket) {
        Map<String, Object> event = new HashMap<>();
        event.put("correlationId", request.getCorrelationId());
        event.put("platform", request.getPlatform());
        event.put("intent", intent.intent());
        event.put("confidence", intent.confidence());
        event.put("ticketId", ticket);
        event.put("userId", request.getUserId());
        event.put("authProtocol", request.getAuthProtocol());
        event.put("challengeId", request.getChallengeId());
        event.put("priority", request.getPriority());
        if (analysis != null) {
            event.put("rootCause", analysis.rootCause());
            event.put("fixAction", analysis.fixAction());
        }
        openSearchClient.indexEvent(eventType, event);
    }
}
