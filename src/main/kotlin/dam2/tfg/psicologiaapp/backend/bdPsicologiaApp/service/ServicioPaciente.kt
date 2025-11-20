package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import org.springframework.stereotype.Service

@Service
class ServicioPaciente(
    val pacienteRepository: PacienteRepository,
    val servicioUsuario: ServicioUsuario,
    val servicioPsicologo: ServicioPsicologo
) {

    fun obtenerPacientes(): List<Paciente>{
        return pacienteRepository.findAll()
    }

    fun obtenerPaciente(firebaseUsuarioId: String): Paciente?{
        return pacienteRepository.findByIdFirebaseUsuario(firebaseUsuarioId)
    }


}