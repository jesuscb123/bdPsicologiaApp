package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse

object TareaMapper {
    fun toEntity(request: TareaCrearRequest, psicologo: Psicologo, paciente: Paciente): Tarea {
        return Tarea(
            tituloTarea = request.titulo,
            descripcionTarea = request.descripcion,
            psicologo = psicologo,
            paciente = paciente
        )
    }

    fun toResponse(entity: Tarea): TareaResponse {
        return TareaResponse(
            id = entity.id ?: throw IllegalStateException("La tarea no tiene ID"),
            titulo = entity.tituloTarea,
            descripcion = entity.descripcionTarea,
            horaEnvio = entity.horaEnvio,
            realizada = entity.realizada,
            psicologo = PsicologoMapper.toResponse(entity.psicologo),
            paciente = PacienteMapper.toResponse(entity.paciente)
        )
    }
}

