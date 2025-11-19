package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    val servicioUsuario: IServicioUsuario
) {

    @GetMapping
    fun obtenerUsuarios(): List<UsuarioResponse>{
        return servicioUsuario.obtenerUsuarios().map(UsuarioMapper::toResponse)
    }

    fun obtenerUsuarioByFireBaseId(fireBaseUid: String): Usuario?{
        return servicioUsuario.obtenerUsuarioByFireBaseId(fireBaseUid)
    }
}