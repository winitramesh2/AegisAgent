package com.aegis.agent.integration;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.AnalysisResult;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier {

    private final JavaMailSender mailSender;
    private final AegisProperties properties;

    public EmailNotifier(JavaMailSender mailSender, AegisProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void notifyEscalation(ChatRequest request, AnalysisResult result) {
        if (properties.getEscalationEmailTo() == null || properties.getEscalationEmailTo().isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(properties.getEscalationEmailTo());
        message.setSubject("[Aegis] Escalation required - " + request.getPlatform());
        message.setText(buildBody(request, result));
        mailSender.send(message);
    }

    private String buildBody(ChatRequest request, AnalysisResult result) {
        return "Aegis escalation\n"
                + "User: " + request.getUserId() + "\n"
                + "Platform: " + request.getPlatform() + "\n"
                + "Query: " + request.getQuery() + "\n"
                + "Root cause: " + result.rootCause() + "\n"
                + "Fix action: " + result.fixAction() + "\n"
                + "Correlation ID: " + request.getCorrelationId();
    }
}
