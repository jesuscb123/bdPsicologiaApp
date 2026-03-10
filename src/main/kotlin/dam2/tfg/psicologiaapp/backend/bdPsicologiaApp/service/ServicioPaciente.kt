package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PacienteMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPaciente(
    val pacienteRepository: PacienteRepository,
    val servicioPsicologo: IServicioPsicologo

) : IServicioPaciente {

    @Transactional
    override fun obtenerPacientes(): List<PacienteResponse>{
       val pacientes =  pacienteRepository.findAll()

        return pacientes.map { PacienteMapper.toResponse(it) }
    }

    @Transactional
    override fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): PacienteResponse?{
        return pacienteRepository.findByIdFirebaseUsuario(firebaseUsuarioId)?.let { PacienteMapper.toResponse(it) }
    }

    override fun obtenerPacienteId(id: Long): PacienteResponse?{
        return pacienteRepository.findByIdOrNull(id)?.let { paciente ->
            PacienteMapper.toResponse(paciente)
        }
    }

    @Transactional
    override fun crearPaciente(usuario: Usuario, pacienteRequest: PacienteRequest): PacienteResponse {
        if (pacienteRepository.existsByUsuario(usuario)) {
            throw IllegalStateException("El usuario ${usuario.nombreUsuario} ya es paciente")
        }
        var psicologoAsociado: Psicologo? = null

        if (pacienteRequest.psicologoId != null) {
            psicologoAsociado = servicioPsicologo.obtenerEntidadPsicologo(pacienteRequest.psicologoId)

            if (usuario.id == psicologoAsociado.usuario.id) {
                throw IllegalStateException("Un usuario no puede ser su propio psicólogo")
            }
        }
        val nuevoPaciente = Paciente(
            usuario = usuario,
            psicologo = psicologoAsociado
        )

        val pacienteGuardado = pacienteRepository.save(nuevoPaciente)

        return PacienteMapper.toResponse(pacienteGuardado)
    }

    override fun obtenerEntidadPacientePorFirebaseId(firebaseId: String): Paciente {
        return pacienteRepository.findByIdFirebaseUsuario(firebaseId)
            ?: throw IllegalStateException("El paciente no existe o el ID de Firebase es incorrecto")
    }

}