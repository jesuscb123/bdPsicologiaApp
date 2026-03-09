package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import com.google.firebase.auth.FirebaseAuthException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Rol
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/psicologos")
class PsicologoController(
    private val servicioPsicologo: IServicioPsicologo,
    private val servicioUsuario: IServicioUsuario,
    private val firebaseService: FirebaseService
) : IController {
    @GetMapping
    fun obtenerPsicologos(): List<PsicologoResponse> {
        return servicioPsicologo.obtenerPsicologos()
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPsicologoByFirebaseId(@PathVariable firebaseId: String): ResponseEntity<PsicologoResponse>? {
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId)
        return if (psicologo != null) {
            ResponseEntity.ok(PsicologoMapper.toResponse(psicologo))
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
            ResponseEntity.ok(PsicologoMapper.toResponse(psicologo))
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun crearPsicologo(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody psicologoRequest: PsicologoRequest // Pedimos el tipo específico
    ): ResponseEntity<Any> {
        return try {
            if (!authorizationHeader.startsWith("Bearer ", ignoreCase = true)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("El formato del token de autorización es incorrecto. Debe ser 'Bearer <token>'.")
            }

            val token = authorizationHeader.substring(7).trim()

            val usuarioFirebase = firebaseService.getUserFromToken(token)
                ?: return errorTokenExpirado()

            val psicologoResponse = servicioUsuario.crearUsuario(
                usuarioFirebase.uid,
                usuarioFirebase.email,
                psicologoRequest
            )

            ResponseEntity.created(URI.create("/api/psicologos/${psicologoResponse.id}"))
                .body(psicologoResponse)

        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)

        } catch (e: FirebaseAuthException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Error al validar el token de Firebase: ${e.message}")

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
    }
}