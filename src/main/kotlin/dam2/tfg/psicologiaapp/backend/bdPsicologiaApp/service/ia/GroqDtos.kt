package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Modelo OpenAI-compatible que usamos al hablar con `POST /chat/completions` de Groq.
 *
 * Solo definimos los campos imprescindibles para nuestro caso de uso (resumen de notas).
 * Cualquier campo extra que devuelva Groq se ignora silenciosamente con
 * `@JsonIgnoreProperties(ignoreUnknown = true)` para no romper si la API evoluciona.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GroqChatMessage(
    val role: String,
    val content: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqChatMessage>,
    @JsonProperty("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null,
    val stream: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroqChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<GroqChoice> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroqChoice(
    val index: Int? = null,
    val message: GroqChatMessage? = null,
    @JsonProperty("finish_reason") val finishReason: String? = null,
)
