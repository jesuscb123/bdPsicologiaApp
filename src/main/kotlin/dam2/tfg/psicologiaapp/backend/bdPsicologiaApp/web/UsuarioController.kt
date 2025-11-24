package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import com.google.firebase.auth.FirebaseAuthException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
   private val servicioUsuario: IServicioUsuario,
    private val firebaseService: FirebaseService
) {

    @GetMapping
    fun obtenerUsuarios(): List<UsuarioResponse>{
        return servicioUsuario.obtenerUsuarios().map(UsuarioMapper::toResponse)
    }

    @GetMapping("/{fireBaseUid}")
    fun obtenerUsuarioByFireBaseId(@PathVariable fireBaseUid: String): ResponseEntity<UsuarioResponse>?{
        val usuario = servicioUsuario.obtenerUsuarioByFireBaseId(fireBaseUid)
        return if (usuario != null){
            ResponseEntity.ok(UsuarioMapper.toResponse(usuario))
        }else{
            null
        }
    }

    // En UsuarioController.kt

    @PostMapping
    fun crearUsuario(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody usuarioRequest: UsuarioRequest
    ): ResponseEntity<Any> {
        try {
            if (!authorizationHeader.startsWith("Bearer ", ignoreCase = true)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("El formato del token de autorización es incorrecto. Debe ser 'Bearer <token>'.")
            }

            val token = authorizationHeader.substring(7).trim()


            val usuarioFirebase = firebaseService.getUserFromToken(token)
                ?: return errorTokenExpirado()

            if (servicioUsuario.obtenerUsuarioByFireBaseId(usuarioFirebase.uid) != null) {
                return errorUsuarioExiste(usuarioFirebase)
            }

            return guardarUsuario(usuarioFirebase, usuarioRequest)

        } catch (e: FirebaseAuthException) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Error al validar el token de Firebase: ${e.message}")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor: ${e.message}")
        }
    }



    private fun guardarUsuario(usuarioFirebase: FirebaseUserData, usuarioRequest: UsuarioRequest): ResponseEntity<Any>{
        val usuarioGuardado = servicioUsuario.crearUsuario(usuarioFirebase.uid, usuarioFirebase.email, usuarioRequest)
        return ResponseEntity.created(URI.create("/api/usuarios/${usuarioGuardado.firebaseUid}")).body(UsuarioMapper.toResponse(usuarioGuardado))
    }

    // FUNCIONES ERRORES
    private fun errorTokenExpirado(): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autorización inválido o expirado.")

    }

    private fun errorUsuarioExiste(usuarioFirebase: FirebaseUserData): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario con ${usuarioFirebase.uid} ya existe.")
    }


}
