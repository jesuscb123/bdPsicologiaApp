package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioResumenIa
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.ResumenIaServicioNoDisponibleException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.iaDTO.ResumenIaResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint público (autenticado) para generar bajo demanda un resumen IA de las últimas
 * notas de un paciente concreto.
 *
 * Mapeo de errores del servicio a códigos HTTP:
 *  - [SecurityException] (el paciente no pertenece al psicólogo) → 403 Forbidden.
 *  - [ResumenIaServicioNoDisponibleException] (Groq sin API key, timeout o error remoto) → 503.
 *  - [IllegalStateException] con mensaje `"Sin notas"` o paciente/psicólogo inexistente → 404.
 *
 * El cuerpo de las respuestas de error se queda vacío para no filtrar detalles internos al
 * cliente; el detalle real vive en los logs del servicio.
 */
@RestController
@RequestMapping("/api/notas/pacientes/{pacienteId}/resumen-ia")
class ResumenIaController(
    private val servicioResumenIa: IServicioResumenIa,
) {

    @PostMapping
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun generarResumen(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long,
    ): ResponseEntity<ResumenIaResponse> {
        return try {
            val resumen = servicioResumenIa.generarResumenNotasPaciente(
                uidPsicologo = usuarioFirebase.uid,
                pacienteId = pacienteId,
            )
            ResponseEntity.ok(resumen)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (e: ResumenIaServicioNoDisponibleException) {
            // Importante: capturar ANTES de IllegalStateException porque hereda de ella.
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}
