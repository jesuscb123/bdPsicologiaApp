package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PacienteMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPsicologo(
   private val psicologoRepository: PsicologoRepository,
   private val pacienteRepository: PacienteRepository
) : IServicioPsicologo{

    private val log = LoggerFactory.getLogger(ServicioPsicologo::class.java)

    @Transactional
    override fun obtenerPsicologos(): List<PsicologoResponse> {
        val psicologos = psicologoRepository.findAll()

        return psicologos.map { PsicologoMapper.toResponse(it) }
    }

    @Transactional
    override fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): PsicologoResponse?{
        return psicologoRepository.findByIdFirebaseUsuario(firebaseUsuarioId)?.let {
            PsicologoMapper.toResponse(it)
        }
    }

    @Transactional
    override fun obtenerPsicologoId(id: Long): PsicologoResponse? {
        return psicologoRepository.findByIdOrNull(id)?.let { psicologo ->
            PsicologoMapper.toResponse(psicologo)
        }
    }

    @Transactional(readOnly = true)
    override fun buscarPsicologosPorNombre(nombreUsuario: String): List<PsicologoResponse> {
        if (nombreUsuario.isBlank()) {
            return emptyList()
        }
        val psicologos = psicologoRepository
            .findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase(
                nombreUsuario.trim(),
                nombreUsuario.trim()
            )
        return psicologos.map { PsicologoMapper.toResponse(it) }
    }

    @Transactional
    override fun crearPsicologo(usuario: Usuario, psicologoRequest: PsicologoRequest): PsicologoResponse {

        if (pacienteRepository.existsByUsuario(usuario)) {
            log.warn(
                "Intento de escalada de privilegios: firebaseUid={} intenta registrarse como PSICOLOGO " +
                "pero ya tiene rol PACIENTE.",
                usuario.firebaseUid
            )
            throw IllegalStateException(
                "No es posible registrarse como psicólogo: la cuenta ya tiene perfil de paciente."
            )
        }

        if (psicologoRepository.existsByUsuario(usuario)){
            throw IllegalStateException("El usuario ${usuario.nombre} ${usuario.apellidos} ya es psicólogo")
        }else{
            log.warn(
                "Alta de psicólogo: firebaseUid={}, numeroColegiado={}",
                usuario.firebaseUid, psicologoRequest.numeroColegiado
            )
            val nuevoPsicologo = Psicologo(
                usuario = usuario,
                numeroColegiado = psicologoRequest.numeroColegiado,
                especialidad = psicologoRequest.especialidad,
                descripcion = psicologoRequest.descripcion
            )
           val psicologo = psicologoRepository.save(nuevoPsicologo)

            return PsicologoMapper.toResponse(psicologo)
        }
    }

    @Transactional(readOnly = true)
    override fun obtenerEntidadPsicologo(id: Long): Psicologo {
        return psicologoRepository.findByIdOrNull(id)
            ?: throw IllegalStateException("El psicólogo con id $id no existe")
    }

    @Transactional(readOnly = true)
    override fun obtenerPacientesPorFirebaseId(firebaseUidPsicologo: String): List<PacienteResponse> {
        val psicologo = psicologoRepository.findByIdFirebaseUsuario(firebaseUidPsicologo)
            ?: return emptyList()
        val pacientes = pacienteRepository.findAllByPsicologo(psicologo)
        return pacientes.map { PacienteMapper.toResponse(it) }
    }

    @Transactional
    override fun actualizarDescripcion(firebaseUidPsicologo: String, descripcion: String?): PsicologoResponse {
        val psicologo = psicologoRepository.findByIdFirebaseUsuario(firebaseUidPsicologo)
            ?: throw IllegalStateException("No existe psicólogo para este usuario")

        val descripcionNormalizada = descripcion
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        val actualizado = psicologoRepository.save(
            psicologo.copy(descripcion = descripcionNormalizada)
        )

        return PsicologoMapper.toResponse(actualizado)
    }

    @Transactional(readOnly = true)
    override fun obtenerPsicologoPorIdConAutorizacion(
        firebaseUidLlamante: String,
        psicologoId: Long
    ): PsicologoResponse {
        val psicologo = psicologoRepository.findByIdOrNull(psicologoId)
            ?: throw IllegalStateException("El psicólogo no existe")
        verificarAccesoPsicologo(firebaseUidLlamante, psicologo)
        return PsicologoMapper.toResponse(psicologo)
    }

    @Transactional(readOnly = true)
    override fun obtenerPsicologoPorFirebaseIdConAutorizacion(
        firebaseUidLlamante: String,
        firebaseUidPsicologo: String
    ): PsicologoResponse {
        val psicologo = psicologoRepository.findByIdFirebaseUsuario(firebaseUidPsicologo)
            ?: throw IllegalStateException("El psicólogo no existe")
        verificarAccesoPsicologo(firebaseUidLlamante, psicologo)
        return PsicologoMapper.toResponse(psicologo)
    }

    /**
     * Regla única de autorización para lecturas individuales de psicólogo:
     *  - el propio psicólogo puede leerse a sí mismo, o
     *  - un paciente cuyo `psicologo_id` apunte al psicólogo solicitado puede leerlo.
     *
     * Cualquier otro caller produce [SecurityException], que el controlador traduce a 403.
     */
    private fun verificarAccesoPsicologo(firebaseUidLlamante: String, psicologo: Psicologo) {
        if (psicologo.usuario.firebaseUid == firebaseUidLlamante) return

        val pacienteLlamante = pacienteRepository.findByIdFirebaseUsuario(firebaseUidLlamante)
        val psicologoAsignado = pacienteLlamante?.psicologo
        if (psicologoAsignado != null && psicologoAsignado.id == psicologo.id) return

        throw SecurityException("No tienes permiso para acceder a este psicólogo.")
    }
}