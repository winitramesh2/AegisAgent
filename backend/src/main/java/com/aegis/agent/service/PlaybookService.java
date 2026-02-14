package com.aegis.agent.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PlaybookService {

    private static final Map<String, List<String>> FIX_ACTIONS = Map.of(
            "EnrollmentFailure", List.of(
                    "Confirm internet connectivity and latest app version.",
                    "Re-run enrollment using QR/manual code.",
                    "Check device date and time are automatic."
            ),
            "GenerateOTP", List.of(
                    "Sync device time automatically.",
                    "Refresh authenticator account and retry OTP.",
                    "Validate server and device timezone alignment."
            ),
            "TokenSyncError", List.of(
                    "Check account token sync status.",
                    "Re-link authenticator profile.",
                    "Clear stale token cache and retry."
            ),
            "ConfigIssue", List.of(
                    "Validate app configuration and environment endpoint.",
                    "Check policy settings for OTP/passkey factors.",
                    "Restart app after config refresh."
            ),
            "ServerUnreachable", List.of(
                    "Verify backend health and endpoint reachability.",
                    "Inspect proxy/firewall settings.",
                    "Retry after brief backoff."
            ),
            "PushApprovalTimeout", List.of(
                    "Enable push notification permission.",
                    "Disable battery optimization for authenticator app.",
                    "Retry challenge on stable network."
            )
    );

    public List<String> actionsFor(String intent) {
        return FIX_ACTIONS.getOrDefault(intent, List.of(
                "Please upload logs for deeper analysis.",
                "Include app version, OS version, and failure timestamp."
        ));
    }
}
