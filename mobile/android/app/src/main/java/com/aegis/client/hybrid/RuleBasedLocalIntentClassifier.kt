package com.aegis.client.hybrid

import java.util.Locale

class RuleBasedLocalIntentClassifier : LocalIntentClassifier {

    private val keywordToIntent = linkedMapOf(
        "otp" to "GenerateOTP",
        "token" to "GenerateOTP",
        "push" to "PushApprovalTimeout",
        "approve" to "PushApprovalTimeout",
        "timeout" to "PushApprovalTimeout",
        "passkey" to "PasskeyRegistrationFailure",
        "webauthn" to "PasskeyRegistrationFailure",
        "biometric" to "BiometricLockout",
        "fingerprint" to "BiometricLockout",
        "face" to "BiometricLockout",
        "enroll" to "EnrollmentFailure",
        "register" to "EnrollmentFailure",
        "sync" to "TokenSyncError",
        "time" to "TimeDriftFailure",
        "clock" to "TimeDriftFailure",
        "server" to "ServerUnreachable",
        "offline" to "ServerUnreachable",
        "config" to "ConfigIssue"
    )

    override fun classify(query: String): LocalIntentPrediction? {
        val normalized = query.trim().lowercase(Locale.ROOT)
        if (normalized.length < 3) {
            return null
        }
        for ((keyword, intent) in keywordToIntent) {
            if (normalized.contains(keyword)) {
                return LocalIntentPrediction(intent, 0.84)
            }
        }
        return null
    }
}
