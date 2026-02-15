package com.aegis.client.hybrid

interface LocalIntentClassifier {
    fun classify(query: String): LocalIntentPrediction?
}
