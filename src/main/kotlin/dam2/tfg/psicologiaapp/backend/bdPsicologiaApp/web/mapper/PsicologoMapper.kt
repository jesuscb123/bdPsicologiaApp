package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse


object PsicologoMapper {
    fun toEntity(request: PsicologoRequest, usuario: Usuario): Psicologo {
        return Psicologo(
            usuario = usuario,
            numeroColegiado = request.numeroColegiado,
            especialidad = request.especialidad,
            descripcion = request.descripcion
        )
    }

    fun toResponse(psicologo: Psicologo): PsicologoResponse {
        return PsicologoResponse(
            id = psicologo.usuario.id ?: throw IllegalStateException("ID nulo"),
            idEntidadPsicologo = psicologo.id ?: throw IllegalStateException("ID entidad psicólogo nulo"),
            firebaseUid = psicologo.usuario.firebaseUid,
            nombre = psicologo.usuario.nombre,
            apellidos = psicologo.usuario.apellidos,
            fotoPerfilUrl = psicologo.usuario.fotoPerfilUrl,

            numeroColegiado = psicologo.numeroColegiado,
            especialidad = psicologo.especialidad,
            descripcion = psicologo.descripcion
        )
    }


}