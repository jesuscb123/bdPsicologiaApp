package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse

object UsuarioMapper {
    fun toEntity(request: UsuarioRequest, fireBaseUid: String, email: String): Usuario{
        return Usuario(
            firebaseUid = fireBaseUid,
            email = email,
            nombreUsuario = request.nombreUsuario
        )
    }

    fun toResponse(usuario: Usuario): UsuarioResponse {
        return UsuarioResponse(
            usuario.id!!,
            usuario.firebaseUid,
            usuario.email,
            usuario.nombreUsuario
        )
    }

    fun merge(entity: Usuario, request: UsuarioRequest): Usuario{
        return entity.copy(
            nombreUsuario = request.nombreUsuario
        )
    }


}