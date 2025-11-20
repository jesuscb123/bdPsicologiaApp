package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioResponse
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
            return null
        }
    }

    @PostMapping
    fun crearUsuario(
        @RequestHeader("Authorization") authorizationHeader: String, // Recibimos el token de la cabecera
        @RequestBody usuarioRequest: UsuarioRequest
    ): ResponseEntity<Any> { // Usamos 'Any' para poder devolver diferentes tipos de respuesta (error o éxito)

        // 1. Verificamos el token y extraemos los datos del usuario de Firebase
        val usuarioFirebase = firebaseService.getUserFromToken(authorizationHeader)
            ?: return errorTokenExpirado()

        // 2.Comprobamos si un usuario con ese UID ya existe en nuestra DB
        if (servicioUsuario.obtenerUsuarioByFireBaseId(usuarioFirebase.uid) != null) {
            return errorUsuarioExiste(usuarioFirebase)
        }

        // 3. Llamamos al servicio con los datos seguros obtenidos del token de Firebase
        // No confiamos en los datos de fireBaseUid y email que el cliente podría enviar en el body.
        return guardarUsuario(usuarioFirebase, usuarioRequest)
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
