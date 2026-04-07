package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioBasicoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse

object UsuarioMapper {
    fun toEntity(request: UsuarioRequest, fireBaseUid: String, email: String): Usuario{
        return Usuario(
            firebaseUid = fireBaseUid,
            email = email,
            nombre = request.nombre,
            apellidos = request.apellidos,
            fotoPerfilUrl = request.fotoPerfilUrl
        )
    }

    fun toResponse(usuario: Usuario): UsuarioBasicoResponse {
        return UsuarioBasicoResponse(
            id = usuario.id ?: throw IllegalStateException("El usuario no tiene ID"),
            firebaseUid = usuario.firebaseUid,
            nombre = usuario.nombre,
            apellidos = usuario.apellidos,
            fotoPerfilUrl = usuario.fotoPerfilUrl
        )
    }

    fun merge(entity: Usuario, request: UsuarioRequest): Usuario{
        return entity.copy(
            nombre = request.nombre,
            apellidos = request.apellidos
        )
    }


}