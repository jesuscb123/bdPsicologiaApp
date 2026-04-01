package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.CrearPsicologoMeRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/psicologos")
class PsicologoController(
    private val servicioPsicologo: IServicioPsicologo,
    private val usuarioRepository: UsuarioRepository
) : IController {
    @GetMapping
    fun obtenerPsicologos(): List<PsicologoResponse> {
        return servicioPsicologo.obtenerPsicologos()
    }

    @PostMapping("/me")
    fun crearPsicologoMe(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: CrearPsicologoMeRequest
    ): ResponseEntity<Any> {
        return try {
            val usuario = usuarioRepository.findByFirebaseUid(usuarioFirebase.uid)
                ?: return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No existe un usuario para este firebaseUid. Crea primero el usuario con POST /api/usuarios.")

            val psicologoRequest = PsicologoRequest(
                nombreUsuario = usuario.nombreUsuario,
                fotoPerfilUrl = usuario.fotoPerfilUrl,
                rol = "PSICOLOGO",
                numeroColegiado = request.numeroColegiado,
                especialidad = request.especialidad
            )

            val creado = servicioPsicologo.crearPsicologo(usuario, psicologoRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(creado)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerMiPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<PsicologoResponse> {
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(usuarioFirebase.uid)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(psicologo)
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('PACIENTE')")
    fun buscarPsicologosPorNombre(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestParam nombreUsuario: String
    ): ResponseEntity<List<PsicologoResponse>> {
        val resultados = servicioPsicologo.buscarPsicologosPorNombre(nombreUsuario)
        return ResponseEntity.ok(resultados)
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPsicologoByFirebaseId(@PathVariable firebaseId: String): ResponseEntity<PsicologoResponse>? {
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId)
        return if (psicologo != null) {
            ResponseEntity.ok(psicologo)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/id/{id}")
    fun obtenerPsicologoById(@PathVariable id: Long?): ResponseEntity<Any> {

        if (id == null) {
            return ResponseEntity.badRequest().body("El ID del psicólogo no puede ser nulo.")
        }

        val psicologo = servicioPsicologo.obtenerPsicologoId(id)

        return if (psicologo != null){
            ResponseEntity.ok(psicologo)
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/me/pacientes")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun obtenerMisPacientes(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<PacienteResponse>> {
        val pacientes = servicioPsicologo.obtenerPacientesPorFirebaseId(usuarioFirebase.uid)
        return if (pacientes.isEmpty()) ResponseEntity.noContent().build()
        else ResponseEntity.ok(pacientes)
    }
}