package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse

object PacienteMapper {

    fun toEntity(request: PacienteRequest, psicologo: Psicologo, usuario: Usuario): Paciente {
        return Paciente(
            usuario = usuario,
            psicologo = psicologo,
        )
    }

    fun toResponse(paciente: Paciente): PacienteResponse {
        return PacienteResponse(
            id = paciente.usuario.id ?: throw IllegalStateException("ID nulo"),
            firebaseUid = paciente.usuario.firebaseUid,
            nombreUsuario = paciente.usuario.nombreUsuario,
            fotoPerfilUrl = paciente.usuario.fotoPerfilUrl,
            psicologoId = paciente.psicologo?.id,
            idPaciente = paciente.id ?: throw IllegalStateException("ID de paciente nulo")
        )
    }

}