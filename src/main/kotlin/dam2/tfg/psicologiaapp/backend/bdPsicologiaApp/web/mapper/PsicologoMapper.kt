package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse


object PsicologoMapper {
    fun toEntity(request: PsicologoRequest, usuario: Usuario): Psicologo {
        return Psicologo(
            usuario = usuario,
            numeroColegiado = request.numeroColegiado,
            especialidad = request.especialidad
        )
    }

    fun toResponse(psicologo: Psicologo): PsicologoResponse {
        return PsicologoResponse(
            id = psicologo.id!!,
            numeroColegiado = psicologo.numeroColegiado,
            especialidad = psicologo.especialidad,
            usuario = UsuarioMapper.toResponse(psicologo.usuario)
        )
    }

}