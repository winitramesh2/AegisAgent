package com.aegis.agent.service;

import com.aegis.agent.domain.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LogAnalysisService {

    public AnalysisResult analyze(String rawLog) {
        String input = rawLog == null ? "" : rawLog;
        String normalized = input.toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();

        if (normalized.contains("error 503") || normalized.contains("http 503")) {
            matches.add("Error503");
            return new AnalysisResult(
                    "Service unavailable or upstream outage",
                    "Check server health, retry after 2 minutes, verify network proxy rules.",
                    "HIGH",
                    0.93,
                    matches
            );
        }
        if (normalized.contains("cert_invalid") || normalized.contains("certificate verify failed")) {
            matches.add("Cert_Invalid");
            return new AnalysisResult(
                    "Device certificate invalid or expired",
                    "Re-register device certificate and confirm trust chain is current.",
                    "HIGH",
                    0.91,
                    matches
            );
        }
        if (normalized.contains("time_skew") || normalized.contains("clock") || normalized.contains("time drift")) {
            matches.add("Time_Skew");
            return new AnalysisResult(
                    "Device time out of sync",
                    "Enable automatic date/time, sync timezone, then regenerate OTP.",
                    "MEDIUM",
                    0.9,
                    matches
            );
        }
        if (normalized.contains("fido2") || normalized.contains("webauthn")) {
            matches.add("FIDO2_WebAuthn_Failure");
            return new AnalysisResult(
                    "Passkey registration or assertion failed",
                    "Re-enroll passkey, verify platform authenticator support, and retry login.",
                    "MEDIUM",
                    0.82,
                    matches
            );
        }
        if (normalized.contains("push") && normalized.contains("timeout")) {
            matches.add("PushApprovalTimeout");
            return new AnalysisResult(
                    "Push approval timed out",
                    "Check push notification permissions and network reachability, then resend challenge.",
                    "MEDIUM",
                    0.84,
                    matches
            );
        }

        return new AnalysisResult(
                "Unknown root cause",
                "Collect additional logs, include timestamp and app version, then escalate.",
                "MEDIUM",
                0.4,
                matches
        );
    }
}
