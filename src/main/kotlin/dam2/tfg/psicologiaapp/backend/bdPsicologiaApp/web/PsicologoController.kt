package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.ActualizarDescripcionPsicologoRequest
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
    /**
     * Listado completo de psicólogos. Restringido a usuarios con rol `PSICOLOGO`
     * (p. ej. para que un psicólogo localice colegas en flujos internos). Antes de
     * esta restricción el endpoint era un IDOR que permitía a cualquier usuario
     * autenticado enumerar el directorio entero de psicólogos. Los pacientes deben
     * usar `GET /api/psicologos/id/{id}` o `/api/psicologos/buscar` en su lugar.
     */
    @GetMapping
    @PreAuthorize("hasRole('PSICOLOGO')")
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
                nombre = usuario.nombre,
                apellidos = usuario.apellidos,
                fotoPerfilUrl = usuario.fotoPerfilUrl,
                rol = "PSICOLOGO",
                numeroColegiado = request.numeroColegiado,
                especialidad = request.especialidad,
                descripcion = null
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

    @PatchMapping("/me/descripcion")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun actualizarMiDescripcion(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: ActualizarDescripcionPsicologoRequest
    ): ResponseEntity<Any> {
        return try {
            val actualizado = servicioPsicologo.actualizarDescripcion(
                firebaseUidPsicologo = usuarioFirebase.uid,
                descripcion = request.descripcion
            )
            ResponseEntity.ok(actualizado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
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

    /**
     * Lectura por firebaseUid del usuario psicólogo. Sólo se permite cuando el llamante
     * coincide con el psicólogo o cuando es un paciente cuyo `psicologo_id` apunta a
     * este psicólogo. Cualquier otro caller obtiene 403.
     */
    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPsicologoByFirebaseId(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable firebaseId: String
    ): ResponseEntity<Any> {
        return try {
            val psicologo = servicioPsicologo.obtenerPsicologoPorFirebaseIdConAutorizacion(
                firebaseUidLlamante = usuarioFirebase.uid,
                firebaseUidPsicologo = firebaseId
            )
            ResponseEntity.ok(psicologo)
        } catch (_: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (_: IllegalStateException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Lectura por id de entidad PSICOLOGOS. Misma regla de autorización que
     * [obtenerPsicologoByFirebaseId].
     */
    @GetMapping("/id/{id}")
    fun obtenerPsicologoById(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable id: Long?
    ): ResponseEntity<Any> {
        if (id == null) {
            return ResponseEntity.badRequest().body("El ID del psicólogo no puede ser nulo.")
        }

        return try {
            val psicologo = servicioPsicologo.obtenerPsicologoPorIdConAutorizacion(
                firebaseUidLlamante = usuarioFirebase.uid,
                psicologoId = id
            )
            ResponseEntity.ok(psicologo)
        } catch (_: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        } catch (_: IllegalStateException) {
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