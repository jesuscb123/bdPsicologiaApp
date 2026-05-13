package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Propiedades de configuración para la integración con Groq (resumen IA de notas).
 *
 * La `apiKey` SIEMPRE debe inyectarse desde una variable de entorno del backend
 * (`GROQ_API_KEY`); no debe persistirse en el repositorio ni viajar al cliente Android.
 * Si llega vacía, el servicio de resumen IA responderá con 503.
 */
@ConfigurationProperties(prefix = "ia.groq")
data class GroqProperties(
    val apiKey: String = "",
    val baseUrl: String = "https://api.groq.com/openai/v1",
    val modelo: String = "llama-3.1-8b-instant",
    val timeoutSegundos: Long = 25,
    val maxTokens: Int = 600,
    val temperatura: Double = 0.3,
)
