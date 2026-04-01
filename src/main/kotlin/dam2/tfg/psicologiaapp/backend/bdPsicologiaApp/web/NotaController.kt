package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/notas")
class NotaController(
    private val servicioNota: IServicioNota
) {
    @GetMapping("/pacientes/{pacienteId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerNotasParaPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<List<NotaResponse>> {
        val notas = servicioNota.obtenerNotasPacienteParaPsicologo(usuarioFirebase.uid, pacienteId)

        return if (notas.isNotEmpty()) {
            ResponseEntity.ok(notas)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerMisNotas(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<NotaResponse>> {

        val notas = servicioNota.obtenerNotasPaciente(usuarioFirebase.uid)

        return if (notas.isNotEmpty()) ResponseEntity.ok(notas) else ResponseEntity.noContent().build()
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    fun crearNota(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestBody request: NotaRequest
    ): ResponseEntity<NotaResponse> {

        return try {
            val notaGuardada = servicioNota.crearNota(usuarioFirebase.uid, request)

            ResponseEntity.created(URI.create("/api/notas/${notaGuardada.id}")).body(notaGuardada)

        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{notaId}")
    @PreAuthorize("hasRole('PACIENTE')")
    fun actualizarNota(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable notaId: Long,
        @RequestBody request: NotaRequest
    ): ResponseEntity<Any> {
        return try {
            val actualizada = servicioNota.actualizarNota(usuarioFirebase.uid, notaId, request)
            ResponseEntity.ok(actualizada)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @DeleteMapping("/{notaId}")
    @PreAuthorize("hasRole('PACIENTE')")
    fun eliminarNota(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable notaId: Long
    ): ResponseEntity<Any> {
        return try {
            servicioNota.eliminarNota(usuarioFirebase.uid, notaId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }
}