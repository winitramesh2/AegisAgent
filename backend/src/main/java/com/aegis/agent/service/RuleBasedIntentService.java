package com.aegis.agent.service;

import com.aegis.agent.domain.IntentResult;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class RuleBasedIntentService implements IntentService {

    private static final Map<String, String> KEYWORDS_TO_INTENT = new LinkedHashMap<>();
    private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile(".*[a-zA-Z0-9].*");

    static {
        KEYWORDS_TO_INTENT.put("enroll", "EnrollmentFailure");
        KEYWORDS_TO_INTENT.put("registration", "EnrollmentFailure");
        KEYWORDS_TO_INTENT.put("otp", "GenerateOTP");
        KEYWORDS_TO_INTENT.put("code not generating", "GenerateOTP");
        KEYWORDS_TO_INTENT.put("token sync", "TokenSyncError");
        KEYWORDS_TO_INTENT.put("time drift", "TimeDriftFailure");
        KEYWORDS_TO_INTENT.put("time skew", "TimeDriftFailure");
        KEYWORDS_TO_INTENT.put("config", "ConfigIssue");
        KEYWORDS_TO_INTENT.put("503", "ServerUnreachable");
        KEYWORDS_TO_INTENT.put("unreachable", "ServerUnreachable");
        KEYWORDS_TO_INTENT.put("push timeout", "PushApprovalTimeout");
        KEYWORDS_TO_INTENT.put("passkey", "PasskeyRegistrationFailure");
        KEYWORDS_TO_INTENT.put("biometric", "BiometricLockout");
        KEYWORDS_TO_INTENT.put("device binding", "DeviceBindingFailure");
    }

    @Override
    public IntentResult classify(String query) {
        if (query == null || query.isBlank()) {
            return new IntentResult("Unknown", 0.0);
        }

        String normalized = query.toLowerCase(Locale.ROOT);
        if (!ALPHA_NUMERIC_PATTERN.matcher(normalized).matches() || normalized.trim().length() < 3) {
            return new IntentResult("Unknown", 0.05);
        }

        for (Map.Entry<String, String> entry : KEYWORDS_TO_INTENT.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return new IntentResult(entry.getValue(), 0.76);
            }
        }
        return new IntentResult("Unknown", 0.2);
    }
}
