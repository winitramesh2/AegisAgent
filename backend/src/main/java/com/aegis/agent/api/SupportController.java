package com.aegis.agent.api;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.api.dto.ChatResponse;
import com.aegis.agent.api.dto.LogAnalysisResponse;
import com.aegis.agent.api.dto.IncidentTimelineResponse;
import com.aegis.agent.api.dto.JiraValidationResponse;
import com.aegis.agent.api.dto.ComponentStatusResponse;
import com.aegis.agent.api.dto.ComponentStatusItem;
import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.AnalysisResult;
import com.aegis.agent.domain.IntentResolution;
import com.aegis.agent.domain.IntentResult;
import com.aegis.agent.integration.JiraClient;
import com.aegis.agent.integration.OpenSearchClient;
import com.aegis.agent.service.EscalationService;
import com.aegis.agent.service.DeepPavlovIntentProvider;
import com.aegis.agent.service.IntentService;
import com.aegis.agent.service.LogAnalysisService;
import com.aegis.agent.service.PlaybookService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class SupportController {

    private static final Pattern LETTER_PATTERN = Pattern.compile("[A-Za-z]");

    private final IntentService intentService;
    private final LogAnalysisService logAnalysisService;
    private final PlaybookService playbookService;
    private final EscalationService escalationService;
    private final AegisProperties properties;
    private final OpenSearchClient openSearchClient;
    private final JiraClient jiraClient;
    private final DeepPavlovIntentProvider deepPavlovIntentProvider;
    private final Environment environment;

    public SupportController(
            IntentService intentService,
            LogAnalysisService logAnalysisService,
            PlaybookService playbookService,
            EscalationService escalationService,
            AegisProperties properties,
            OpenSearchClient openSearchClient,
            JiraClient jiraClient,
            DeepPavlovIntentProvider deepPavlovIntentProvider,
            Environment environment
    ) {
        this.intentService = intentService;
        this.logAnalysisService = logAnalysisService;
        this.playbookService = playbookService;
        this.escalationService = escalationService;
        this.properties = properties;
        this.openSearchClient = openSearchClient;
        this.jiraClient = jiraClient;
        this.deepPavlovIntentProvider = deepPavlovIntentProvider;
        this.environment = environment;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String correlationId = request.getCorrelationId() == null || request.getCorrelationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.getCorrelationId();
        request.setCorrelationId(correlationId);

        IntentResolution resolution = intentService.classifyResolution(request.getQuery(), request.isRetryAttempt());
        IntentResult intent = resolution.getPrimaryIntent();

        ChatResponse response = new ChatResponse();
        response.setCorrelationId(correlationId);

        if (isLowInformationQuery(request.getQuery())) {
            response.setIntent("Unknown");
            response.setConfidence(0.0);
            response.setStatus("NEED_MORE_INFO");
            response.setMessage("Please describe the problem with at least one symptom (for example: OTP invalid, push timeout, passkey failure). ");
            response.setActions(List.of(
                    "Include app name and platform (Android/iOS/Desktop)",
                    "Mention exact error text if visible",
                    "Share when the issue started"
            ));
            indexChatEvent("CHAT_NEED_MORE_INFO", request, new IntentResult("Unknown", 0.0), null, null);
            return response;
        }

        response.setIntent(intent.intent());
        response.setConfidence(intent.confidence());

        response.setStatus("GUIDED");
        response.setMessage(buildDiagnosisMessage(resolution, request.isRetryAttempt()));
        response.setActions(refinedActions(resolution));
        indexChatEvent("CHAT_GUIDED", request, intent, null, null);
        return response;
    }

    private String buildDiagnosisMessage(IntentResolution resolution, boolean retryAttempt) {
        String primary = resolution.getPrimaryIntent().intent();
        String source = resolution.getSourceSummary();
        boolean cloudBacked = source != null && source.toLowerCase().contains("cloud");
        String base;
        if (retryAttempt) {
            base = cloudBacked
                    ? "Retry diagnosis from cloud model: probable issue is " + primary + "."
                    : "Retry diagnosis used fallback logic: probable issue is " + primary + ".";
        } else {
            base = cloudBacked
                    ? "Primary diagnosis from cloud model: probable issue is " + primary + "."
                    : "Primary diagnosis from local fallback: probable issue is " + primary + ".";
        }
        if (resolution.hasSecondaryIntent()) {
            return base + " DeepPavlov cross-check also suggests " + resolution.getSecondaryIntent().intent() + ".";
        }
        return base + " Source: " + resolution.getSourceSummary() + ".";
    }

    private List<String> refinedActions(IntentResolution resolution) {
        LinkedHashSet<String> merged = new LinkedHashSet<>(playbookService.actionsFor(resolution.getPrimaryIntent().intent()));
        if (resolution.hasSecondaryIntent()) {
            merged.addAll(playbookService.actionsFor(resolution.getSecondaryIntent().intent()));
        }
        return merged.stream().limit(5).toList();
    }

    private boolean isLowInformationQuery(String query) {
        if (query == null) {
            return true;
        }
        String trimmed = query.trim();
        return trimmed.length() < 3 || !LETTER_PATTERN.matcher(trimmed).find();
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
        response.setMessage("Issue escalated successfully. Please wait for 3 working days and the support team will contact you.");
        response.setActions(List.of("Ticket ID: " + ticket, "Support team SLA: 3 working days"));

        indexChatEvent("MANUAL_ESCALATION", request, new IntentResult("Escalation", 1.0), analysis, ticket);
        return response;
    }

    @PostMapping(value = "/escalate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatResponse escalateJson(@Valid @RequestBody ChatRequest request) {
        String correlationId = request.getCorrelationId() == null || request.getCorrelationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.getCorrelationId();
        request.setCorrelationId(correlationId);

        AnalysisResult analysis = logAnalysisService.analyze(request.getQuery());
        String ticket = escalationService.escalate(request, analysis, null, null);

        ChatResponse response = new ChatResponse();
        response.setStatus("ESCALATED");
        response.setIntent("Escalation");
        response.setConfidence(1.0);
        response.setCorrelationId(correlationId);
        response.setEscalationTicketId(ticket);
        response.setMessage("Issue escalated successfully. Please wait for 3 working days and the support team will contact you.");
        response.setActions(List.of("Ticket ID: " + ticket, "Support team SLA: 3 working days"));

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

    @GetMapping("/incidents")
    public IncidentTimelineResponse incidentTimelineByFilters(
            @RequestParam(value = "correlationId", required = false) String correlationId,
            @RequestParam(value = "platform", required = false) String platform,
            @RequestParam(value = "eventType", required = false) String eventType,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "size", defaultValue = "50") int size
    ) {
        List<Map<String, Object>> events = openSearchClient.timelineByFilters(correlationId, platform, eventType, from, to, size);
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

    @GetMapping("/status/components")
    public ComponentStatusResponse componentStatus() {
        Map<String, ComponentStatusItem> components = new HashMap<>();
        components.put("backend", new ComponentStatusItem("UP", "http://localhost:8080/actuator/health", "Core API"));
        components.put("deeppavlov", new ComponentStatusItem(
                deepPavlovIntentProvider.isHealthy() ? "UP" : "DOWN",
                properties.getDeeppavlovUrl(),
                "Intent inference"
        ));
        components.put("opensearch", new ComponentStatusItem(
                openSearchClient.isHealthy() ? "UP" : "DOWN",
                properties.getOpenSearchUrl(),
                "Log indexing and replay"
        ));
        components.put("jira", new ComponentStatusItem(
                jiraClient.isHealthy() ? "UP" : "DOWN",
                properties.getJiraBaseUrl(),
                "Ticket escalation"
        ));

        String smtpHost = environment.getProperty("spring.mail.host", "");
        int smtpPort = Integer.parseInt(environment.getProperty("spring.mail.port", "587"));
        boolean smtpUp = isSmtpReachable(smtpHost, smtpPort);
        components.put("email", new ComponentStatusItem(
                smtpUp ? "UP" : "DOWN",
                smtpHost.isBlank() ? null : "smtp://" + smtpHost + ":" + smtpPort,
                "Escalation email"
        ));

        ComponentStatusResponse response = new ComponentStatusResponse();
        response.setComponents(components);
        return response;
    }

    private boolean isSmtpReachable(String host, int port) {
        if (host == null || host.isBlank()) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500);
            return true;
        } catch (IOException ex) {
            return false;
        }
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
