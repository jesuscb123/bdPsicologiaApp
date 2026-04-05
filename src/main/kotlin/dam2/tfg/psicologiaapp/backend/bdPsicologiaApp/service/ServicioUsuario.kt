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
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PacienteMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI

@Service
class ServicioUsuario(
    private val usuarioRepository: UsuarioRepository,
    private val psicologoRepository: PsicologoRepository,
    private val pacienteRepository: PacienteRepository,
    private val servicioPsicologo: IServicioPsicologo,
    private val servicioPaciente: IServicioPaciente,
    private val servicioAlmacenamientoFotoPerfil: ServicioAlmacenamientoFotoPerfil,
) : IServicioUsuario {

    /**
     * Resuelve el response del usuario según roles existentes en BD.
     * Si coexisten roles, prioriza PSICOLOGO (misma lógica que obtenerPerfilUsuario).
     */
    private fun resolverUsuarioResponse(usuario: Usuario): UsuarioResponse {
        val firebaseUid = usuario.firebaseUid

        psicologoRepository.findByIdFirebaseUsuario(firebaseUid)?.let { psicologo ->
            return PsicologoMapper.toResponse(psicologo)
        }

        pacienteRepository.findByIdFirebaseUsuario(firebaseUid)?.let { paciente ->
            return PacienteMapper.toResponse(paciente)
        }

        return UsuarioMapper.toResponse(usuario)
    }

    @Transactional
    override fun obtenerUsuarioByFireBaseId(idFirebase: String): UsuarioResponse? {
        return usuarioRepository.findByFirebaseUid(idFirebase)?.let { usuario ->
            resolverUsuarioResponse(usuario)
        }
    }

    @Transactional(readOnly = true)
    override fun obtenerUsuarios(): List<UsuarioResponse> {
        val usuarios = usuarioRepository.findAll()

        return usuarios.map { usuario ->
            resolverUsuarioResponse(usuario)
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

    @Transactional
    override fun actualizarEmailUsuario(firebaseUid: String, nuevoEmail: String): UsuarioPerfilResponse {
        val usuario = usuarioRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("El usuario no existe")

        require(nuevoEmail.isNotBlank()) { "El email no puede estar vacío" }

        if (!EMAIL_REGEX.matches(nuevoEmail)) {
            throw IllegalArgumentException("El formato del email no es válido")
        }

        if (usuario.email == nuevoEmail) {
            return obtenerPerfilUsuario(firebaseUid)
        }

        if (usuarioRepository.existsByEmail(nuevoEmail)) {
            throw IllegalStateException("El email ya está en uso")
        }

        usuario.email = nuevoEmail
        usuarioRepository.save(usuario)

        return obtenerPerfilUsuario(firebaseUid)
    }

    @Transactional
    override fun actualizarFotoPerfilUsuario(firebaseUid: String, fotoPerfilUrl: String): UsuarioPerfilResponse {
        val usuario = usuarioRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("El usuario no existe")

        val urlNormalizada = fotoPerfilUrl.trim()
        require(urlNormalizada.isNotEmpty()) { "La URL de la foto de perfil no puede estar vacía" }

        if (!esUrlFotoPerfilValida(urlNormalizada)) {
            throw IllegalArgumentException("La URL de la foto de perfil no es válida")
        }

        if (usuario.fotoPerfilUrl == urlNormalizada) {
            return obtenerPerfilUsuario(firebaseUid)
        }

        usuario.fotoPerfilUrl = urlNormalizada
        usuarioRepository.save(usuario)

        return obtenerPerfilUsuario(firebaseUid)
    }

    @Transactional
    override fun subirFotoPerfilDesdeArchivo(
        firebaseUid: String,
        bytes: ByteArray,
        tipoContenido: String?,
    ): UsuarioPerfilResponse {
        usuarioRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("El usuario no existe")
        val tipoNormalizado = tipoContenido?.trim()?.takeIf { it.startsWith("image/", ignoreCase = true) }
            ?: throw IllegalArgumentException("El archivo debe ser una imagen (Content-Type image/*)")
        val urlPublica = servicioAlmacenamientoFotoPerfil.guardar(bytes, tipoNormalizado)
        return actualizarFotoPerfilUsuario(firebaseUid, urlPublica)
    }

    @Transactional
    override fun eliminarUsuario(firebaseUid: String) {
        val usuario = usuarioRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalStateException("El usuario no existe")

        pacienteRepository.findByIdFirebaseUsuario(firebaseUid)?.let { paciente ->
            pacienteRepository.delete(paciente)
        }

        psicologoRepository.findByIdFirebaseUsuario(firebaseUid)?.let { psicologo ->
            val pacientesAsociados = pacienteRepository.findAllByPsicologo(psicologo)
            if (pacientesAsociados.isNotEmpty()) {
                pacientesAsociados.forEach { it.psicologo = null }
                pacienteRepository.saveAll(pacientesAsociados)
            }
            psicologoRepository.delete(psicologo)
        }

        usuarioRepository.delete(usuario)
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

    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        private fun esUrlFotoPerfilValida(url: String): Boolean =
            try {
                val uri = URI(url)
                val esquema = uri.scheme?.lowercase()
                (esquema == "http" || esquema == "https") && !uri.host.isNullOrBlank()
            } catch (_: Exception) {
                false
            }
    }
}