package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPaciente(
    val pacienteRepository: PacienteRepository,
    val servicioUsuario: IServicioUsuario,
    val servicioPsicologo: ServicioPsicologo
) : IServicioPaciente {

    @Transactional
    override fun obtenerPacientes(): List<Paciente>{
        return pacienteRepository.findAll()
    }

    @Transactional
    override fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): Paciente?{
        return pacienteRepository.findByIdFirebaseUsuario(firebaseUsuarioId)
    }

    override fun obtenerPacienteId(id: Long): Paciente?{
        return pacienteRepository.findById(id).orElse(null)
    }

    @Transactional
    override fun crearPaciente(firebaseUsuarioId: String, pacienteRequest: PacienteRequest):Paciente?{
        val usuarioExiste = servicioUsuario.obtenerUsuarioByFireBaseId(firebaseUsuarioId) ?: throw IllegalStateException("No se puede crear un perfil para un usuario inexistente: ${firebaseUsuarioId}")

        if (pacienteRepository.existsByUsuario(usuarioExiste)) return null

        var psicologoAsociado: Psicologo? = null
        if (pacienteRequest.psicologoId != null){
            psicologoAsociado = servicioPsicologo.obtenerPsicologoId(pacienteRequest.psicologoId) ?: throw IllegalStateException("El psicólogo con id ${pacienteRequest.psicologoId} no existe}")
        }

        val nuevoPaciente = Paciente(
            usuario = usuarioExiste,
            psicologo = psicologoAsociado
        )
        return pacienteRepository.save(nuevoPaciente)
    }
}