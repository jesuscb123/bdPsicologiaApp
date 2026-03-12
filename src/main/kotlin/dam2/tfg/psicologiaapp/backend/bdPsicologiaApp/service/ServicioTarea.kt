package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.TareaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRealizadaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.TareaMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioTarea(
    private val tareaRepository: TareaRepository,
    private val psicologoRepository: PsicologoRepository,
    private val pacienteRepository: PacienteRepository
) : IServicioTarea {

    @Transactional
    override fun crearTarea(firebaseUidPsicologo: String, pacienteId: Long, request: TareaCrearRequest): TareaResponse {
        val psicologo = obtenerPsicologoPorFirebaseUid(firebaseUidPsicologo)
        val paciente = obtenerPacientePorId(pacienteId)

        val psicologoPacienteId = paciente.psicologo?.id
            ?: throw IllegalStateException("El paciente no tiene psicólogo asignado")
        if (psicologoPacienteId != psicologo.id) {
            throw SecurityException("No tienes permiso para asignar tareas a este paciente")
        }

        val nuevaTarea = TareaMapper.toEntity(request, psicologo, paciente)
        val guardada = tareaRepository.save(nuevaTarea)
        return TareaMapper.toResponse(guardada)
    }

    @Transactional(readOnly = true)
    override fun obtenerTareasPaciente(firebaseUidPaciente: String): List<TareaResponse> {
        val tareas = tareaRepository.findTareasByPacienteFirebaseUid(firebaseUidPaciente)
        return tareas.map { TareaMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun obtenerTareasPacienteParaPsicologo(firebaseUidPsicologo: String, pacienteId: Long): List<TareaResponse> {
        // Si no es psicólogo, este método fallará al buscarlo
        obtenerPsicologoPorFirebaseUid(firebaseUidPsicologo)
        val tareas = tareaRepository.findTareasByPsicologoFirebaseUidAndPacienteId(firebaseUidPsicologo, pacienteId)
        return tareas.map { TareaMapper.toResponse(it) }
    }

    @Transactional
    override fun actualizarRealizada(
        firebaseUidPaciente: String,
        tareaId: Long,
        request: TareaActualizarRealizadaRequest
    ): TareaResponse {
        val tarea = tareaRepository.findByIdOrNull(tareaId)
            ?: throw IllegalStateException("La tarea no existe")

        if (tarea.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para actualizar esta tarea")
        }

        tarea.realizada = request.realizada
        val actualizada = tareaRepository.save(tarea)
        return TareaMapper.toResponse(actualizada)
    }

    @Transactional
    override fun actualizarTarea(
        firebaseUidPsicologo: String,
        tareaId: Long,
        request: TareaActualizarRequest
    ): TareaResponse {
        val tarea = tareaRepository.findByIdOrNull(tareaId)
            ?: throw IllegalStateException("La tarea no existe")

        if (tarea.psicologo.usuario.firebaseUid != firebaseUidPsicologo) {
            throw SecurityException("No tienes permiso para actualizar esta tarea")
        }

        tarea.tituloTarea = request.titulo
        tarea.descripcionTarea = request.descripcion
        val actualizada = tareaRepository.save(tarea)
        return TareaMapper.toResponse(actualizada)
    }

    private fun obtenerPsicologoPorFirebaseUid(firebaseUid: String): Psicologo {
        return psicologoRepository.findByIdFirebaseUsuario(firebaseUid)
            ?: throw SecurityException("No autorizado: el usuario no es psicólogo")
    }

    private fun obtenerPacientePorId(pacienteId: Long): Paciente {
        return pacienteRepository.findByIdOrNull(pacienteId)
            ?: throw IllegalStateException("El paciente no existe")
    }
}

