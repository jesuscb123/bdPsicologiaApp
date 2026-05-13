package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.iaDTO

import java.time.LocalDateTime

/**
 * Respuesta pública del endpoint `POST /api/notas/pacientes/{id}/resumen-ia`.
 *
 * Importante: NO incluye nombres, ids, emails ni el contenido íntegro de las notas;
 * solo el texto del resumen producido por la IA y metadatos no sensibles.
 */
data class ResumenIaResponse(
    val resumen: String,
    val numeroNotasAnalizadas: Int,
    val generadoEn: LocalDateTime,
    val modelo: String,
)
