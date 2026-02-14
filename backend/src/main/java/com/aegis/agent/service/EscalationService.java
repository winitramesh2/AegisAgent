package com.aegis.agent.service;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.domain.AnalysisResult;
import com.aegis.agent.integration.EmailNotifier;
import com.aegis.agent.integration.JiraClient;
import org.springframework.stereotype.Service;

@Service
public class EscalationService {

    private final JiraClient jiraClient;
    private final EmailNotifier emailNotifier;

    public EscalationService(JiraClient jiraClient, EmailNotifier emailNotifier) {
        this.jiraClient = jiraClient;
        this.emailNotifier = emailNotifier;
    }

    public String escalate(ChatRequest request, AnalysisResult analysisResult, byte[] rawLog, String fileName) {
        emailNotifier.notifyEscalation(request, analysisResult);
        return jiraClient.createTicketAndAttachLog(request, analysisResult, rawLog, fileName);
    }
}
