package com.kam666.mealplanner.data.remote.ai

interface AiApiService {
    suspend fun complete(systemPrompt: String, userMessage: String): String

    suspend fun completeWithImage(
        systemPrompt: String,
        imageBase64: String,
        mimeType: String,
        userMessage: String = ""
    ): String
}
