package com.example.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "parts") val parts: List<GeminiPart>,
    @param:Json(name = "role") val role: String? = null
) {
    companion object {
        fun fromText(text: String, role: String? = null): GeminiContent {
            return GeminiContent(
                parts = listOf(GeminiPart(text = text)),
                role = role
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @param:Json(name = "temperature") val temperature: Float? = null,
    @param:Json(name = "topP") val topP: Float? = null,
    @param:Json(name = "topK") val topK: Int? = null,
    @param:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @param:Json(name = "responseMimeType") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @param:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @param:Json(name = "promptFeedback") val promptFeedback: GeminiPromptFeedback? = null
) {
    val firstText: String?
        get() = candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
}

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @param:Json(name = "content") val content: GeminiContent? = null,
    @param:Json(name = "finishReason") val finishReason: String? = null,
    @param:Json(name = "index") val index: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPromptFeedback(
    @param:Json(name = "blockReason") val blockReason: String? = null
)
