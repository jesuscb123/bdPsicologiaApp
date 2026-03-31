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
) : IController{
    @GetMapping
    fun obtenerPacientes(): List<PacienteResponse>{
        return servicioPaciente.obtenerPacientes()
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
                nombreUsuario = usuario.nombreUsuario,
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

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun buscarPacientesPorNombre(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestParam nombreUsuario: String
    ): ResponseEntity<List<PacienteResponse>> {
        val resultados = servicioPaciente.buscarPacientesPorNombre(nombreUsuario)
        return ResponseEntity.ok(resultados)
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPacienteByFirebaseId(firebaseId: String): ResponseEntity<PacienteResponse>{
        val paciente = servicioPaciente.obtenerPacienteFirebaseId(firebaseId)

       return if (paciente != null){
            ResponseEntity.ok(paciente)
        }else{
            ResponseEntity.notFound().build()
       }
    }

    @GetMapping("/id/{id}")
    fun obtenerPacienteById(@PathVariable id: Long?): ResponseEntity<Any> {
        if (id == null ) return ResponseEntity.badRequest().body("El id de paciente no puede ser nulo")

        val paciente = servicioPaciente.obtenerPacienteId(id)
        return if (paciente != null){
            ResponseEntity.ok(paciente)
        }else{
            ResponseEntity.notFound().build()
        }
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