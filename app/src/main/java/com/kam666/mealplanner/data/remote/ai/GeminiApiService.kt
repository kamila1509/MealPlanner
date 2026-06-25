package com.kam666.mealplanner.data.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val GEMINI_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

class GeminiApiService(
    private val client: HttpClient,
    private val apiKey: String
) : AiApiService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(systemPrompt: String, userMessage: String): String {
        val combinedPrompt = "$systemPrompt\n\n$userMessage"
        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = combinedPrompt)))
            )
        )
        val url = "$GEMINI_URL?key=$apiKey"
        val response: GeminiResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GeminiRequest.serializer(), request))
        }.body()
        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }

    override suspend fun completeWithImage(
        systemPrompt: String,
        imageBase64: String,
        mimeType: String,
        userMessage: String
    ): String {
        val combinedPrompt = if (userMessage.isNotBlank()) "$systemPrompt\n\n$userMessage" else systemPrompt
        val requestBody = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject { put("text", combinedPrompt) })
                        add(buildJsonObject {
                            put("inline_data", buildJsonObject {
                                put("mime_type", mimeType)
                                put("data", imageBase64)
                            })
                        })
                    })
                })
            })
        }
        val url = "$GEMINI_URL?key=$apiKey"
        val response: GeminiResponse = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonElement.serializer(), requestBody))
        }.body()
        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
    }
}

@Serializable
private data class GeminiRequest(val contents: List<GeminiContent>)

@Serializable
private data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
private data class GeminiPart(val text: String)

@Serializable
private data class GeminiResponse(val candidates: List<GeminiCandidate>)

@Serializable
private data class GeminiCandidate(val content: GeminiContent)
