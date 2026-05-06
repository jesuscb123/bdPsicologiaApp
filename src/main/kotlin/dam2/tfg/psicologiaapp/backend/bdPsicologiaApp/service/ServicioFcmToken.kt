package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FcmToken
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.FcmTokenRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioFcmToken(
    private val fcmTokenRepository: FcmTokenRepository,
    private val usuarioRepository: UsuarioRepository,
) : IServicioFcmToken {

    private val log = LoggerFactory.getLogger(ServicioFcmToken::class.java)

    @Transactional
    override fun registrarToken(
        firebaseUidUsuario: String,
        token: String,
        instalacionId: String?,
        plataforma: String?,
    ) {
        val tokenLimpio = token.trim()
        if (tokenLimpio.isEmpty()) throw IllegalArgumentException("El token FCM no puede estar vacío")

        val usuario = usuarioRepository.findByFirebaseUid(firebaseUidUsuario)
            ?: throw IllegalStateException("Usuario no encontrado para uid $firebaseUidUsuario")
        val usuarioId = usuario.id
            ?: throw IllegalStateException("Usuario sin id no se puede asociar al token FCM")

        val plataformaNormalizada = (plataforma ?: "ANDROID").trim().uppercase()

        val existente = fcmTokenRepository.findByToken(tokenLimpio)
        if (existente != null) {
            // Reasignamos el token al usuario actual: el dispositivo puede haber cambiado de cuenta.
            existente.usuario = usuario
            existente.instalacionId = instalacionId
            existente.plataforma = plataformaNormalizada
            fcmTokenRepository.save(existente)
        } else {
            val nuevo = FcmToken(
                usuario = usuario,
                token = tokenLimpio,
                instalacionId = instalacionId,
                plataforma = plataformaNormalizada,
            )
            fcmTokenRepository.save(nuevo)
        }

        // Si tenemos id de instalación, descartamos tokens viejos de esa misma instalación
        // (típicamente Firebase rota el token y nos manda solo el nuevo).
        if (!instalacionId.isNullOrBlank()) {
            val borrados = fcmTokenRepository.deleteOtrosDeMismaInstalacion(
                usuarioId = usuarioId,
                instalacionId = instalacionId,
                tokenAExcluir = tokenLimpio,
            )
            if (borrados > 0 && log.isDebugEnabled) {
                log.debug("Limpiados {} tokens viejos de instalación {}", borrados, instalacionId)
            }
        }
    }

    @Transactional
    override fun eliminarToken(firebaseUidUsuario: String, token: String) {
        val tokenLimpio = token.trim()
        if (tokenLimpio.isEmpty()) return

        val existente = fcmTokenRepository.findByToken(tokenLimpio) ?: return
        // Solo el propietario puede borrar su propio token.
        if (existente.usuario.firebaseUid != firebaseUidUsuario) {
            log.warn(
                "Intento de borrar token FCM ajeno: token pertenece a otro uid. Petición ignorada.",
            )
            return
        }
        fcmTokenRepository.deleteByToken(tokenLimpio)
    }

    @Transactional(readOnly = true)
    override fun obtenerTokensDe(firebaseUid: String): List<String> =
        fcmTokenRepository.findAllByUsuarioFirebaseUid(firebaseUid).map { it.token }

    @Transactional
    override fun invalidarToken(token: String) {
        val tokenLimpio = token.trim()
        if (tokenLimpio.isEmpty()) return
        fcmTokenRepository.deleteByToken(tokenLimpio)
    }
}
