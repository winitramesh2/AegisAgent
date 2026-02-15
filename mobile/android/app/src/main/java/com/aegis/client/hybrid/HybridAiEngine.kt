package com.aegis.client.hybrid

class HybridAiEngine(
    private val classifier: LocalIntentClassifier,
    private val repository: ResponsePackRepository,
    private val minConfidence: Double = 0.8
) {

    fun diagnose(query: String): HybridDiagnosis? {
        val prediction = classifier.classify(query) ?: return null
        if (prediction.confidence < minConfidence) {
            return null
        }

        val pack = repository.loadPack() ?: return null
        val response = pack.intents.firstOrNull { it.intent.equals(prediction.intent, ignoreCase = true) }
            ?: return HybridDiagnosis(
                intent = prediction.intent,
                confidence = prediction.confidence,
                diagnosis = "Local model detected intent but no response pack entry was found.",
                actions = listOf("Sync response packs", "Retry in Core AI mode"),
                source = "on-device"
            )

        return HybridDiagnosis(
            intent = response.intent,
            confidence = prediction.confidence,
            diagnosis = response.diagnosis,
            actions = response.actions,
            source = "on-device"
        )
    }
}
