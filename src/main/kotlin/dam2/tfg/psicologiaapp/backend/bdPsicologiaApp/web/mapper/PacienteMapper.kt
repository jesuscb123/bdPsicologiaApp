package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteResponse

object PacienteMapper {

    fun toEntity(request: PacienteRequest, psicologo: Psicologo, usuario: Usuario): Paciente {
        return Paciente(
            usuario = usuario,
            psicologo = psicologo,
        )
    }

    fun toResponse(paciente: Paciente): PacienteResponse {
        return PacienteResponse(
            id = paciente.id!!,
            psicologo = PsicologoMapper.toResponse(paciente.psicologo!!),
            usuario = UsuarioMapper.toResponse(paciente.usuario)
        )
    }

}