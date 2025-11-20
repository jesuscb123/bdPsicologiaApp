package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
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

        if (paciente.psicologo?.id == psicologo.id) throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")

        return notaRepository.obtenerNotasPacienteParaPsicologo(paciente, psicologo)
    }

    override fun obtenerNotasPaciente(firebaseId: String): Nota?{
        return notaRepository.obtenerByPacienteUsuarioFirebaseId(firebaseId)

    }

}