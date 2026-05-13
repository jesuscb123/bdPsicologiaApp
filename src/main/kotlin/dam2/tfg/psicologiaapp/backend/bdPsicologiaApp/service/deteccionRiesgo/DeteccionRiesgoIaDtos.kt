package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * DTO al que parseamos la respuesta JSON estructurada de Groq cuando clasifica el riesgo.
 *
 * Esperamos un objeto con forma:
 * ```json
 * { "nivel": "NINGUNO|BAJO|MEDIO|ALTO", "justificacion": "texto corto en español" }
 * ```
 * Cualquier campo extra se ignora; si la respuesta no es JSON válida, lo trata `ServicioDeteccionRiesgo`.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeteccionRiesgoIaPayload(
    val nivel: String? = null,
    val justificacion: String? = null,
)
