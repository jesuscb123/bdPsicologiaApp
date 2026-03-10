package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse

object NotaMapper {
    fun toResponse(nota: Nota): NotaResponse {
        return NotaResponse(
            id = nota.id,
            asunto = nota.asunto,
            descripcion = nota.descripcion,
            paciente = PacienteMapper.toResponse(nota.paciente),
            psicologo = PsicologoMapper.toResponse(nota.psicologo)
        )
    }

    fun toEntity(request: NotaRequest, pacienteAsignado: Paciente, psicologoAsignado: Psicologo): Nota {
        return Nota(
            asunto = request.asunto,
            descripcion = request.descripcion,
            paciente = pacienteAsignado,
            psicologo = psicologoAsignado
        )
    }

    fun toRequest(nota: Nota): NotaRequest {
        return NotaRequest(
            asunto = nota.asunto,
            descripcion = nota.descripcion
        )
    }
}