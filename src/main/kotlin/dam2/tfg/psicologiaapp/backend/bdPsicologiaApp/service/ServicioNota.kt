package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException

@Service
class ServicioNota(
    val notaRepository: NotaRepository,
    val servicioPaciente: IServicioPaciente,
    val servicioPsicologo: IServicioPsicologo,
) : IServicioNota {


    @Transactional(readOnly = true)
    override fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<NotaResponse>{
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId) ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId) ?: throw IllegalStateException("El paciente no existe")

        if (paciente.psicologoId != psicologo.id) throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")

        val notas = notaRepository.obtenerNotasPacienteParaPsicologo(paciente.id, psicologo.id)

        return notas.map { NotaMapper.toResponse(it) }
    }
    override fun obtenerNotasPaciente(firebaseId: String): List<NotaResponse>{
        val notas = notaRepository.obtenerNotasByPacienteUsuarioFirebaseId(firebaseId)

        return notas.map { NotaMapper.toResponse(it) }
    }

    @Transactional
    override fun crearNota(firebaseId: String, notaRequest: NotaRequest): NotaResponse {

        val paciente = servicioPaciente.obtenerEntidadPacientePorFirebaseId(firebaseId)

        val psicologoAsignado = paciente.psicologo
            ?: throw IllegalStateException("No puedes crear una nota porque no tienes un psicólogo asignado")

        val nuevaNota = NotaMapper.toEntity(notaRequest, paciente, psicologoAsignado)

        val notaGuardada = notaRepository.save(nuevaNota)

        return NotaMapper.toResponse(notaGuardada)
    }

    @Transactional
    override fun actualizarNota(firebaseUidPaciente: String, notaId: Long, request: NotaRequest): NotaResponse {
        val nota = notaRepository.findByIdOrNull(notaId)
            ?: throw IllegalStateException("La nota no existe")

        if (nota.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para actualizar esta nota")
        }

        nota.asunto = request.asunto
        nota.descripcion = request.descripcion
        val actualizada = notaRepository.save(nota)
        return NotaMapper.toResponse(actualizada)
    }

    @Transactional
    override fun eliminarNota(firebaseUidPaciente: String, notaId: Long) {
        val nota = notaRepository.findByIdOrNull(notaId)
            ?: throw IllegalStateException("La nota no existe")

        if (nota.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para eliminar esta nota")
        }

        notaRepository.delete(nota)
    }
}