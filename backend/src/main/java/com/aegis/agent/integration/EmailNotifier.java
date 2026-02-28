package com.aegis.agent.integration;

import com.aegis.agent.api.dto.ChatRequest;
import com.aegis.agent.config.AegisProperties;
import com.aegis.agent.domain.AnalysisResult;
import com.aegis.agent.service.SensitiveDataSanitizer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier {

    private final JavaMailSender mailSender;
    private final AegisProperties properties;
    private final SensitiveDataSanitizer sanitizer;

    public EmailNotifier(JavaMailSender mailSender, AegisProperties properties, SensitiveDataSanitizer sanitizer) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.sanitizer = sanitizer;
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
                + "UserRef: " + sanitizer.pseudonymize(request.getUserId()) + "\n"
                + "Platform: " + request.getPlatform() + "\n"
                + "Query: " + sanitizer.sanitize(request.getQuery()) + "\n"
                + "Root cause: " + sanitizer.sanitize(result.rootCause()) + "\n"
                + "Fix action: " + sanitizer.sanitize(result.fixAction()) + "\n"
                + "Correlation ID: " + request.getCorrelationId();
    }
}
