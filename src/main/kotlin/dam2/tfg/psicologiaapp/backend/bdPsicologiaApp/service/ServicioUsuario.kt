package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioUsuario(
    private val usuarioRepository: UsuarioRepository,
    private val servicioPsicologo: IServicioPsicologo,
    private val servicioPaciente: IServicioPaciente
    ) : IServicioUsuario {
    @Transactional
    override fun obtenerUsuarioByFireBaseId(idFirebase: String): UsuarioResponse? {
        return usuarioRepository.findByFirebaseUid(idFirebase)?.let {
            UsuarioMapper.toResponse(it)
        }
    }

    @Transactional(readOnly = true)
    override fun obtenerUsuarios(): List<UsuarioResponse> {
        val usuarios = usuarioRepository.findAll()

        return usuarios.map { usuario ->
            UsuarioMapper.toResponse(usuario)
        }
    }

    @Transactional
    override fun crearUsuario(fireBaseUid: String, email: String, request: UsuarioRequest): UsuarioResponse {

        val usuarioEntidad = obtenerOCrearEntidadUsuario(fireBaseUid, email, request)

        return when (request) {
            is PsicologoRequest -> servicioPsicologo.crearPsicologo(usuarioEntidad, request)
            is PacienteRequest -> servicioPaciente.crearPaciente(usuarioEntidad, request)
        }
    }

    private fun obtenerOCrearEntidadUsuario(fireBaseUid: String, email: String, request: UsuarioRequest): Usuario {
        val usuarioExistente = usuarioRepository.findByFirebaseUid(fireBaseUid)

        if (usuarioExistente != null) {
            return usuarioExistente
        }

        require(email.isNotEmpty()) { "El email no puede estar vacío" }

        val nuevoUsuario = UsuarioMapper.toEntity(request, fireBaseUid, email)
        return usuarioRepository.save(nuevoUsuario)
    }
}