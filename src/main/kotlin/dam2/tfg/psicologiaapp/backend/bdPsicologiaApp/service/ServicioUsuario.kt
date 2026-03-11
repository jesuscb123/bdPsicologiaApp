package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilBasicoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacientePerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoPerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper.toResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioUsuario(
    private val usuarioRepository: UsuarioRepository,
    private val psicologoRepository: PsicologoRepository,
    private val pacienteRepository: PacienteRepository,
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

    @Transactional(readOnly = true)
    override fun obtenerPerfilUsuario(firebaseUid: String): UsuarioPerfilResponse {
        val usuario = usuarioRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("El usuario no existe")

        psicologoRepository.findByIdFirebaseUsuario(firebaseUid)?.let { psicologo ->
            return PsicologoPerfilResponse(
                id = usuario.id ?: throw IllegalStateException("El usuario no tiene ID"),
                firebaseUid = usuario.firebaseUid,
                nombreUsuario = usuario.nombreUsuario,
                email = usuario.email,
                fotoPerfilUrl = usuario.fotoPerfilUrl,
                numeroColegiado = psicologo.numeroColegiado,
                especialidad = psicologo.especialidad
            )
        }

        pacienteRepository.findByIdFirebaseUsuario(firebaseUid)?.let { paciente ->
            return PacientePerfilResponse(
                id = usuario.id ?: throw IllegalStateException("El usuario no tiene ID"),
                firebaseUid = usuario.firebaseUid,
                nombreUsuario = usuario.nombreUsuario,
                email = usuario.email,
                fotoPerfilUrl = usuario.fotoPerfilUrl,
                psicologoId = paciente.psicologo?.id
            )
        }

        return UsuarioPerfilBasicoResponse(
            id = usuario.id ?: throw IllegalStateException("El usuario no tiene ID"),
            firebaseUid = usuario.firebaseUid,
            nombreUsuario = usuario.nombreUsuario,
            email = usuario.email,
            fotoPerfilUrl = usuario.fotoPerfilUrl
        )
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