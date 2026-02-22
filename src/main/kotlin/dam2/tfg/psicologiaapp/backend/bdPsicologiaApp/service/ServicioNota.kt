package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
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
    override fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<Nota>{
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId) ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId) ?: throw IllegalStateException("El paciente no existe")

        if (paciente.psicologo?.id != psicologo.id) throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")

        return notaRepository.obtenerNotasPacienteParaPsicologo(paciente, psicologo)
    }

    override fun obtenerNotasPaciente(firebaseId: String): Nota?{
        return notaRepository.obtenerByPacienteUsuarioFirebaseId(firebaseId)
    }

    @Transactional
    override fun crearNota(firebaseId: String, request: NotaRequest): Nota {
        val paciente = servicioPaciente.obtenerPacienteFirebaseId(firebaseId)
            ?: throw IllegalStateException("El paciente no existe o el ID de Firebase es incorrecto")

        val psicologoAsignado = paciente.psicologo
            ?: throw IllegalStateException("No puedes crear una nota porque no tienes un psicólogo asignado")

        val nuevaNota = NotaMapper.toEntity(request, paciente, psicologoAsignado)

        return notaRepository.save(nuevaNota)
    }
}