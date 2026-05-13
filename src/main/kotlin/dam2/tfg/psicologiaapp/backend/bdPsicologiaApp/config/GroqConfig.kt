package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.ClientHttpRequestFactories
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.time.Duration

/**
 * Configuración del cliente HTTP hacia la API de Groq.
 *
 * Registra un `RestClient` calificado con `@Bean("groqRestClient")` preconfigurado con:
 *  - URL base de Groq (OpenAI-compatible).
 *  - Cabecera `Authorization: Bearer <apiKey>` (la key se queda SIEMPRE en el backend).
 *  - Timeouts de conexión/lectura idénticos a `timeoutSegundos`.
 *
 * Nota de seguridad: si `apiKey` está vacía no se aborta el arranque para no romper el resto
 * del backend; en su lugar el servicio que use este bean devolverá 503 en tiempo de petición.
 */
@Configuration
@EnableConfigurationProperties(GroqProperties::class, RiesgoIaProperties::class)
class GroqConfig(
    private val propiedades: GroqProperties,
) {

    private val log = LoggerFactory.getLogger(GroqConfig::class.java)

    @Bean("groqRestClient")
    fun groqRestClient(): RestClient {
        if (propiedades.apiKey.isBlank()) {
            log.warn(
                "GROQ_API_KEY no está configurada — los endpoints de resumen IA responderán 503 " +
                "hasta que se defina la variable de entorno."
            )
        } else {
            log.info(
                "RestClient de Groq inicializado (baseUrl={}, modelo={}, timeoutSegundos={})",
                propiedades.baseUrl,
                propiedades.modelo,
                propiedades.timeoutSegundos,
            )
        }

        val ajustes = ClientHttpRequestFactorySettings.DEFAULTS
            .withConnectTimeout(Duration.ofSeconds(propiedades.timeoutSegundos))
            .withReadTimeout(Duration.ofSeconds(propiedades.timeoutSegundos))
        val fabricaPeticiones = ClientHttpRequestFactories.get(ajustes)

        return RestClient.builder()
            .baseUrl(propiedades.baseUrl)
            .requestFactory(fabricaPeticiones)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeaders { cabeceras ->
                if (propiedades.apiKey.isNotBlank()) {
                    cabeceras.setBearerAuth(propiedades.apiKey)
                }
            }
            .build()
    }
}
