package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPaciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.pacienteDTO.CrearPacienteMeRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.pacienteDTO.AsignarPsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pacientes")
class PacienteController(
    private val servicioPaciente: IServicioPaciente,
    private val usuarioRepository: UsuarioRepository
) {
    /**
     * Listado de pacientes. Restringido a psicólogos y filtrado al psicólogo autenticado:
     * cada psicólogo sólo ve sus propios pacientes (los que tienen `paciente.psicologo_id`
     * apuntando a su entidad). Antes de esta restricción el endpoint era un IDOR que
     * permitía a cualquier usuario autenticado listar todos los pacientes.
     */
    @GetMapping
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerPacientes(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<PacienteResponse>> {
        val pacientes = servicioPaciente.obtenerPacientesAsignadosA(usuarioFirebase.uid)
        return ResponseEntity.ok(pacientes)
    }

    @PostMapping("/me")
    fun crearPacienteMe(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: CrearPacienteMeRequest
    ): ResponseEntity<Any> {
        return try {
            val usuario = usuarioRepository.findByFirebaseUid(usuarioFirebase.uid)
                ?: return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No existe un usuario para este firebaseUid. Crea primero el usuario con POST /api/usuarios.")

            val pacienteRequest = PacienteRequest(
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                fotoPerfilUrl = usuario.fotoPerfilUrl,
                rol = "PACIENTE",
                psicologoId = request.psicologoId
            )

            val creado = servicioPaciente.crearPaciente(usuario, pacienteRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(creado)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PACIENTE')")
    fun obtenerMiPaciente(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<PacienteResponse> {
        val paciente = servicioPaciente.obtenerPacienteFirebaseId(usuarioFirebase.uid)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(paciente)
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun buscarPacientesPorNombre(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestParam nombreUsuario: String
    ): ResponseEntity<List<PacienteResponse>> {
        val resultados = servicioPaciente.buscarPacientesPorNombre(nombreUsuario)
        return ResponseEntity.ok(resultados)
    }

    /**
     * Lectura por firebaseUid del usuario paciente. Sólo se permite cuando el llamante
     * coincide con el paciente o cuando es el psicólogo asignado a ese paciente.
     */
    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPacienteByFirebaseId(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable firebaseId: String
    ): ResponseEntity<Any> {
        val paciente = servicioPaciente.obtenerPacientePorFirebaseIdConAutorizacion(
            firebaseUidLlamante = usuarioFirebase.uid,
            firebaseUidPaciente = firebaseId
        )
        return ResponseEntity.ok(paciente)
    }

    /**
     * Lectura por id de entidad PACIENTES_v2. Misma regla de autorización que
     * [obtenerPacienteByFirebaseId].
     */
    @GetMapping("/id/{id}")
    fun obtenerPacienteById(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable id: Long?
    ): ResponseEntity<Any> {
        if (id == null) return ResponseEntity.badRequest().body("El id de paciente no puede ser nulo")
        val paciente = servicioPaciente.obtenerPacientePorIdConAutorizacion(
            firebaseUidLlamante = usuarioFirebase.uid,
            pacienteId = id
        )
        return ResponseEntity.ok(paciente)
    }

    @PatchMapping("/me/psicologo")
    @PreAuthorize("hasRole('PACIENTE')")
    fun asignarPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: AsignarPsicologoRequest
    ): ResponseEntity<Any> {
        return try {
            val paciente = servicioPaciente.actualizarPsicologo(usuarioFirebase.uid, request.psicologoId)
            ResponseEntity.ok(paciente)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }
}
