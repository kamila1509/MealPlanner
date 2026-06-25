package com.kam666.mealplanner.data.remote.ai

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val OPENAI_URL = "https://api.openai.com/v1/chat/completions"

class OpenAiApiService(
    private val client: HttpClient,
    private val apiKey: String,
    private val model: String = "gpt-4o-mini"
) : AiApiService {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun complete(systemPrompt: String, userMessage: String): String {
        val request = OpenAiRequest(
            model = model,
            messages = listOf(
                OpenAiMessage(role = "system", content = systemPrompt),
                OpenAiMessage(role = "user", content = userMessage)
            )
        )
        val response: OpenAiResponse = client.post(OPENAI_URL) {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(OpenAiRequest.serializer(), request))
        }.body()
        return response.choices.firstOrNull()?.message?.content ?: ""
    }

    override suspend fun completeWithImage(
        systemPrompt: String,
        imageBase64: String,
        mimeType: String,
        userMessage: String
    ): String {
        val userContent = buildJsonArray {
            if (userMessage.isNotBlank()) {
                add(buildJsonObject {
                    put("type", "text")
                    put("text", userMessage)
                })
            }
            add(buildJsonObject {
                put("type", "image_url")
                put("image_url", buildJsonObject {
                    put("url", "data:$mimeType;base64,$imageBase64")
                })
            })
        }
        val requestBody = buildJsonObject {
            put("model", model)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                add(buildJsonObject {
                    put("role", "user")
                    put("content", userContent)
                })
            })
        }
        val response: OpenAiResponse = client.post(OPENAI_URL) {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonElement.serializer(), requestBody))
        }.body()
        return response.choices.firstOrNull()?.message?.content ?: ""
    }
}

@Serializable
private data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>
)

@Serializable
private data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
private data class OpenAiResponse(
    val choices: List<OpenAiChoice>
)

@Serializable
private data class OpenAiChoice(
    val message: OpenAiMessage
)
