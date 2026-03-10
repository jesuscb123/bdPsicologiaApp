package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
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
}