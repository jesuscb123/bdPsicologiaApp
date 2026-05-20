package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioTarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.EstadoSyncResponse
import jakarta.validation.Valid
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRealizadaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse
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
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerMisTareas(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<TareaResponse>> {
        val tareas = servicioTarea.obtenerTareasPaciente(usuarioFirebase.uid)
        return if (tareas.isNotEmpty()) ResponseEntity.ok(tareas) else ResponseEntity.noContent().build()
    }

    @GetMapping("/estado")
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerEstadoMisTareas(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<EstadoSyncResponse> {
        val estado = servicioTarea.obtenerEstadoTareasPaciente(usuarioFirebase.uid)
        return ResponseEntity.ok(estado)
    }

    // PSICÓLOGO: ver tareas que ha asignado a un paciente concreto
    @GetMapping("/pacientes/{pacienteId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerTareasPacienteParaPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<List<TareaResponse>> {
        val tareas = servicioTarea.obtenerTareasPacienteParaPsicologo(usuarioFirebase.uid, pacienteId)
        return if (tareas.isNotEmpty()) ResponseEntity.ok(tareas) else ResponseEntity.noContent().build()
    }

    @GetMapping("/pacientes/{pacienteId}/estado")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerEstadoTareasPacienteParaPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<EstadoSyncResponse> {
        val estado = servicioTarea.obtenerEstadoTareasPacienteParaPsicologo(usuarioFirebase.uid, pacienteId)
        return ResponseEntity.ok(estado)
    }

    // PSICÓLOGO: asignar una tarea a un paciente
    @PostMapping("/pacientes/{pacienteId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun crearTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long,
        @Valid @RequestBody request: TareaCrearRequest
    ): ResponseEntity<Any> {
        val creada = servicioTarea.crearTarea(usuarioFirebase.uid, pacienteId, request)
        return ResponseEntity.created(URI.create("/api/tareas/${creada.id}")).body(creada)
    }

    // PACIENTE: marcar tarea como realizada/no realizada
    @PatchMapping("/{tareaId}/realizada")
    @PreAuthorize("hasRole('PACIENTE')")
    fun actualizarRealizada(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long,
        @RequestBody request: TareaActualizarRealizadaRequest
    ): ResponseEntity<TareaResponse> {
        return try {
            val actualizada = servicioTarea.actualizarRealizada(usuarioFirebase.uid, tareaId, request)
            ResponseEntity.ok(actualizada)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

    // PACIENTE: aceptar la tarea asignada por el psicólogo
    @PatchMapping("/{tareaId}/aceptada")
    @PreAuthorize("hasRole('PACIENTE')")
    fun aceptarTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long
    ): ResponseEntity<TareaResponse> {
        val actualizada = servicioTarea.aceptarTareaPaciente(usuarioFirebase.uid, tareaId)
        return ResponseEntity.ok(actualizada)
    }

    // PSICÓLOGO: actualizar título y descripción de una tarea
    @PutMapping("/{tareaId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun actualizarTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long,
        @Valid @RequestBody request: TareaActualizarRequest
    ): ResponseEntity<Any> {
        val actualizada = servicioTarea.actualizarTarea(usuarioFirebase.uid, tareaId, request)
        return ResponseEntity.ok(actualizada)
    }

    // PSICÓLOGO: eliminar una tarea propia
    @DeleteMapping("/{tareaId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun eliminarTarea(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable tareaId: Long
    ): ResponseEntity<Any> {
        servicioTarea.eliminarTarea(usuarioFirebase.uid, tareaId)
        return ResponseEntity.noContent().build()
    }
}

