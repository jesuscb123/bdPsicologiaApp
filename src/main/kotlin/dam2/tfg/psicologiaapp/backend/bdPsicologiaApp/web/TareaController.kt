package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioTarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRealizadaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/tareas")
class TareaController(
    private val servicioTarea: IServicioTarea
) {
    // PACIENTE: ver mis tareas asignadas
    @GetMapping
    fun obtenerMisTareas(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<TareaResponse>> {
        val tareas = servicioTarea.obtenerTareasPaciente(usuarioFirebase.uid)
        return if (tareas.isNotEmpty()) ResponseEntity.ok(tareas) else ResponseEntity.noContent().build()
    }

    // PSICÓLOGO: ver tareas que ha asignado a un paciente concreto
    @GetMapping("/pacientes/{pacienteId}")
    fun obtenerTareasPacienteParaPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<List<TareaResponse>> {
        val tareas = servicioTarea.obtenerTareasPacienteParaPsicologo(usuarioFirebase.uid, pacienteId)
        return if (tareas.isNotEmpty()) ResponseEntity.ok(tareas) else ResponseEntity.noContent().build()
    }

    // PSICÓLOGO: asignar una tarea a un paciente
    @PostMapping("/pacientes/{pacienteId}")
    fun crearTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long,
        @RequestBody request: TareaCrearRequest
    ): ResponseEntity<TareaResponse> {
        val creada = servicioTarea.crearTarea(usuarioFirebase.uid, pacienteId, request)
        return ResponseEntity.created(URI.create("/api/tareas/${creada.id}")).body(creada)
    }

    // PACIENTE: marcar tarea como realizada/no realizada
    @PatchMapping("/{tareaId}/realizada")
    fun actualizarRealizada(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long,
        @RequestBody request: TareaActualizarRealizadaRequest
    ): ResponseEntity<TareaResponse> {
        val actualizada = servicioTarea.actualizarRealizada(usuarioFirebase.uid, tareaId, request)
        return ResponseEntity.ok(actualizada)
    }

    // PSICÓLOGO: actualizar título y descripción de una tarea
    @PutMapping("/{tareaId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun actualizarTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long,
        @RequestBody request: TareaActualizarRequest
    ): ResponseEntity<Any> {
        return try {
            val actualizada = servicioTarea.actualizarTarea(usuarioFirebase.uid, tareaId, request)
            ResponseEntity.ok(actualizada)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    // PSICÓLOGO: eliminar una tarea propia
    @DeleteMapping("/{tareaId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun eliminarTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long
    ): ResponseEntity<Any> {
        return try {
            servicioTarea.eliminarTarea(usuarioFirebase.uid, tareaId)
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

