package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.DisponibilidadResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.LocalDate

@RestController
@RequestMapping("/api/citas")
class CitaController(
    private val servicioCita: IServicioCita
) {
    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerDisponibilidad(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestParam fecha: LocalDate,
        @RequestParam zonaHoraria: String,
    ): ResponseEntity<DisponibilidadResponse> {
        return try {
            val respuesta = servicioCita.obtenerDisponibilidadDia(usuarioFirebase.uid, fecha, zonaHoraria)
            if (respuesta.horasDisponibles.isNotEmpty()) ResponseEntity.ok(respuesta) else ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    fun reservarCita(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestBody request: CitaCrearRequest
    ): ResponseEntity<Any> {
        return try {
            val creada = servicioCita.reservarCita(usuarioFirebase.uid, request)
            ResponseEntity.created(URI.create("/api/citas/${creada.id}")).body(creada)
        } catch (e: IllegalStateException) {
            if (e.message == "CONFLICTO_CITA_SLOT") {
                ResponseEntity.status(HttpStatus.CONFLICT).body("El slot ya está reservado")
            } else {
                ResponseEntity.badRequest().body(e.message)
            }
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerMisCitasPaciente(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<CitaResponse>> {
        val citas = servicioCita.obtenerMisCitasPaciente(usuarioFirebase.uid)
        return if (citas.isNotEmpty()) ResponseEntity.ok(citas) else ResponseEntity.noContent().build()
    }

    @GetMapping("/psicologo")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerMisCitasPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<CitaResponse>> {
        val citas = servicioCita.obtenerMisCitasPsicologo(usuarioFirebase.uid)
        return if (citas.isNotEmpty()) ResponseEntity.ok(citas) else ResponseEntity.noContent().build()
    }

    @PatchMapping("/{citaId}/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    fun cancelarCita(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable citaId: Long
    ): ResponseEntity<Any> {
        return try {
            val cancelada = servicioCita.cancelarCita(usuarioFirebase.uid, citaId)
            ResponseEntity.ok(cancelada)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }
}

