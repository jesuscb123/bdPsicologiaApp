package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.ActualizarEmailRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
@RequestMapping("/api/usuarios")
class UsuarioController(
   private val servicioUsuario: IServicioUsuario,
) {

    @GetMapping
    fun obtenerUsuarios(): List<UsuarioResponse>{
        return servicioUsuario.obtenerUsuarios()
    }

    @GetMapping("/me")
    fun obtenerMiPerfil(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<UsuarioPerfilResponse> {
        val perfil = servicioUsuario.obtenerPerfilUsuario(usuarioFirebase.uid)
        return ResponseEntity.ok(perfil)
    }

    @PatchMapping("/me/email")
    fun actualizarMiEmail(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestBody request: ActualizarEmailRequest
    ): ResponseEntity<Any> {
        return try {
            val perfilActualizado =
                servicioUsuario.actualizarEmailUsuario(usuarioFirebase.uid, request.nuevoEmail)
            ResponseEntity.ok(perfilActualizado)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
    }

    @DeleteMapping("/me")
    fun eliminarMiUsuario(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<Any> {
        return try {
            servicioUsuario.eliminarUsuario(usuarioFirebase.uid)
            ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
    }

    @GetMapping("/{fireBaseUid}")
    fun obtenerUsuarioByFireBaseId(@PathVariable fireBaseUid: String): ResponseEntity<UsuarioResponse>{
        val usuario = servicioUsuario.obtenerUsuarioByFireBaseId(fireBaseUid)
        return if (usuario != null){
            ResponseEntity.ok(usuario)
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun crearUsuario(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestBody usuarioRequest: UsuarioRequest
    ): ResponseEntity<Any> {
        return try {
            if (servicioUsuario.obtenerUsuarioByFireBaseId(usuarioFirebase.uid) != null) {
                return errorUsuarioExiste(usuarioFirebase)
            }

            val usuarioGuardado = servicioUsuario.crearUsuario(
                fireBaseUid = usuarioFirebase.uid,
                email = usuarioFirebase.email,
                request = usuarioRequest
            )

            ResponseEntity.created(URI.create("/api/usuarios/${usuarioGuardado.firebaseUid}"))
                .body(usuarioGuardado)

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
    }

    private fun errorUsuarioExiste(usuarioFirebase: FirebaseUserData): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario con ${usuarioFirebase.uid} ya existe.")
    }


}
