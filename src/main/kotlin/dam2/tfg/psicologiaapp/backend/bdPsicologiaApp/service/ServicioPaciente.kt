package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PacienteMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPaciente(
    val pacienteRepository: PacienteRepository,
    val servicioPsicologo: IServicioPsicologo

) : IServicioPaciente {

    @Transactional
    override fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): PacienteResponse?{
        return pacienteRepository.findByIdFirebaseUsuario(firebaseUsuarioId)?.let { PacienteMapper.toResponse(it) }
    }

    override fun obtenerPacienteId(id: Long): PacienteResponse?{
        return pacienteRepository.findByIdOrNull(id)?.let { paciente ->
            PacienteMapper.toResponse(paciente)
        }
    }

    @Transactional(readOnly = true)
    override fun buscarPacientesPorNombre(nombreUsuario: String): List<PacienteResponse> {
        if (nombreUsuario.isBlank()) {
            return emptyList()
        }

        val pacientes = pacienteRepository
            .findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase(
                nombreUsuario.trim(),
                nombreUsuario.trim()
            )

        return pacientes.map { PacienteMapper.toResponse(it) }
    }

    @Transactional
    override fun crearPaciente(usuario: Usuario, pacienteRequest: PacienteRequest): PacienteResponse {
        if (pacienteRepository.existsByUsuario(usuario)) {
            throw IllegalStateException("El usuario ${usuario.nombre} ${usuario.apellidos} ya es paciente")
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

    @Transactional
    override fun actualizarPsicologo(firebaseUidPaciente: String, psicologoId: Long): PacienteResponse {
        val paciente = pacienteRepository.findByIdFirebaseUsuario(firebaseUidPaciente)
            ?: throw IllegalStateException("El paciente no existe o el ID de Firebase es incorrecto")
        val psicologo = servicioPsicologo.obtenerEntidadPsicologo(psicologoId)
        if (paciente.usuario.id == psicologo.usuario.id) {
            throw IllegalStateException("Un usuario no puede ser su propio psicólogo")
        }
        paciente.psicologo = psicologo
        val guardado = pacienteRepository.save(paciente)
        return PacienteMapper.toResponse(guardado)
    }

    @Transactional(readOnly = true)
    override fun obtenerPacientesAsignadosA(firebaseUidPsicologo: String): List<PacienteResponse> {
        // Delegamos en el servicio de psicólogos para reutilizar la query que ya filtra
        // por la relación PACIENTES_v2.psicologo_id.
        return servicioPsicologo.obtenerPacientesPorFirebaseId(firebaseUidPsicologo)
    }

    @Transactional(readOnly = true)
    override fun obtenerPacientePorIdConAutorizacion(
        firebaseUidLlamante: String,
        pacienteId: Long
    ): PacienteResponse {
        val paciente = pacienteRepository.findByIdConPsicologoYUsuarios(pacienteId)
            ?: throw IllegalStateException("El paciente no existe")
        verificarAccesoPaciente(firebaseUidLlamante, paciente)
        return PacienteMapper.toResponse(paciente)
    }

    @Transactional(readOnly = true)
    override fun obtenerPacientePorFirebaseIdConAutorizacion(
        firebaseUidLlamante: String,
        firebaseUidPaciente: String
    ): PacienteResponse {
        val paciente = pacienteRepository.findByIdFirebaseUsuario(firebaseUidPaciente)
            ?: throw IllegalStateException("El paciente no existe")
        verificarAccesoPaciente(firebaseUidLlamante, paciente)
        return PacienteMapper.toResponse(paciente)
    }

    /**
     * Regla única de autorización para lecturas individuales de paciente:
     *  - el propio paciente puede leerse a sí mismo, o
     *  - el psicólogo asignado al paciente puede leerlo.
     *
     * Cualquier otro caller produce [SecurityException], que el controlador traduce a 403.
     */
    private fun verificarAccesoPaciente(firebaseUidLlamante: String, paciente: Paciente) {
        if (paciente.usuario.firebaseUid == firebaseUidLlamante) return

        val psicologoAsignado = paciente.psicologo
        if (psicologoAsignado != null && psicologoAsignado.usuario.firebaseUid == firebaseUidLlamante) return

        throw SecurityException("No tienes permiso para acceder a este paciente.")
    }
}
