package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    val servicioUsuario: IServicioUsuario
) {

    @GetMapping
    fun obtenerUsuarios(): List<UsuarioResponse>{
        return servicioUsuario.obtenerUsuarios().map(UsuarioMapper::toResponse)
    }

    @GetMapping("/{fireBaseUid}")
    fun obtenerUsuarioByFireBaseId(@PathVariable fireBaseUid: String): Usuario?{
        return servicioUsuario.obtenerUsuarioByFireBaseId(fireBaseUid)
    }

    @PostMapping
    fun crearUsuario(@RequestBody request: UsuarioRequest, fireBaseUid: String, email: String): ResponseEntity<UsuarioResponse> {
        val usuarioGuardado = servicioUsuario.crearUsuario(fireBaseUid, email, request)

        return ResponseEntity.created(URI.create("/api/usuarios/${usuarioGuardado.id}"))
            .body(UsuarioMapper.toResponse(usuarioGuardado))
    }
}