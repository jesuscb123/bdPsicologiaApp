package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.ActualizarEmailRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.ExisteCorreoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import java.net.URI

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
   private val servicioUsuario: IServicioUsuario,
) {

    @GetMapping("/existe-email")
    fun existeCorreo(@RequestParam email: String): ResponseEntity<Any> {
        val existe = servicioUsuario.existeCorreo(email)
        return ResponseEntity.ok(ExisteCorreoResponse(existe = existe))
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
        @Valid @RequestBody request: ActualizarEmailRequest
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

    @PostMapping("/me/foto", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun subirMiFotoPerfil(
        request: HttpServletRequest,
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestPart("archivo") archivo: MultipartFile,
    ): ResponseEntity<Any> {
        if (archivo.isEmpty) {
            return ResponseEntity.badRequest().body("El archivo está vacío")
        }
        val basePublica = OrigenHttpPeticion.basePublica(request)
        val perfilActualizado = servicioUsuario.subirFotoPerfilDesdeArchivo(
            firebaseUid = usuarioFirebase.uid,
            bytes = archivo.bytes,
            tipoContenido = archivo.contentType,
            basePublicaOrigen = basePublica,
        )
        return ResponseEntity.ok(perfilActualizado)
    }

    @DeleteMapping("/me")
    fun eliminarMiUsuario(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<Any> {
        servicioUsuario.eliminarUsuario(usuarioFirebase.uid)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{fireBaseUid}")
    @PreAuthorize("#fireBaseUid == principal.uid")
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
        @Valid @RequestBody usuarioRequest: UsuarioRequest
    ): ResponseEntity<Any> {
        if (servicioUsuario.obtenerUsuarioByFireBaseId(usuarioFirebase.uid) != null) {
            return errorUsuarioExiste(usuarioFirebase)
        }
        val usuarioGuardado = servicioUsuario.crearUsuario(
            fireBaseUid = usuarioFirebase.uid,
            email = usuarioFirebase.email,
            request = usuarioRequest
        )
        return ResponseEntity.created(URI.create("/api/usuarios/${usuarioGuardado.firebaseUid}"))
            .body(usuarioGuardado)
    }

    private fun errorUsuarioExiste(usuarioFirebase: FirebaseUserData): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario con ${usuarioFirebase.uid} ya existe.")
    }


}
