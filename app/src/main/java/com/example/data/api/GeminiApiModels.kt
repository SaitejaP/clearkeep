package com.example.data.api

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mimeType: String,
    val data: String // Base64 encoded string
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String, // "OBJECT", "ARRAY", "STRING", etc.
    val properties: Map<String, SchemaProperty>? = null,
    val required: List<String>? = null,
    val items: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class SchemaProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// Standard Response format parsed from the Gemini analysis
@JsonClass(generateAdapter = true)
data class ReshareAnalysisResult(
    val isSocialMediaReshare: Boolean,
    val category: String, // "Meme", "Promo", "Receipt", "Screenshot", "Personal", "News", "Other"
    val intent: String, // "Entertain", "Broadcast", "Inform", "Personal", "Other"
    val socialMediaApp: String, // "WhatsApp", "Instagram", "TikTok", "Facebook", "Twitter", "None"
    val confidence: Float,
    val aiAnalysisReason: String
)
