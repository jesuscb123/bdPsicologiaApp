package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ServicioNotificacionesPush(
    @Autowired(required = false) private val firebaseApp: FirebaseApp?,
    private val servicioFcmToken: IServicioFcmToken,
) : IServicioNotificacionesPush {

    private val log = LoggerFactory.getLogger(ServicioNotificacionesPush::class.java)

    override fun notificarNuevoMensajeChat(
        firebaseUidDestinatario: String,
        chatId: String,
        nombreRemitente: String,
        vistaPreviaTexto: String,
        pacienteId: Long,
        psicologoId: Long,
    ) {
        val titulo = nombreRemitente.ifBlank { "Nuevo mensaje" }
        val cuerpo = vistaPreviaTexto.take(140)
        val datos = mapOf(
            CLAVE_TIPO to TIPO_CHAT,
            CLAVE_CHAT_ID to chatId,
            CLAVE_PACIENTE_ID to pacienteId.toString(),
            CLAVE_PSICOLOGO_ID to psicologoId.toString(),
        )
        enviarATokensDeUsuario(
            firebaseUidDestinatario = firebaseUidDestinatario,
            titulo = titulo,
            cuerpo = cuerpo,
            canal = CANAL_CHAT,
            datos = datos,
            tag = "chat-$chatId",
        )
    }

    override fun notificarNuevaTarea(
        firebaseUidPaciente: String,
        nombrePsicologo: String,
        tituloTarea: String,
        descripcionTarea: String,
        tareaId: Long,
    ) {
        val titulo = if (nombrePsicologo.isBlank()) {
            "Nueva tarea"
        } else {
            "Nueva tarea de $nombrePsicologo"
        }
        val cuerpo = if (descripcionTarea.isNotBlank()) {
            "$tituloTarea — ${descripcionTarea.take(120)}"
        } else {
            tituloTarea
        }
        val datos = mapOf(
            CLAVE_TIPO to TIPO_TAREA,
            CLAVE_TAREA_ID to tareaId.toString(),
        )
        enviarATokensDeUsuario(
            firebaseUidDestinatario = firebaseUidPaciente,
            titulo = titulo,
            cuerpo = cuerpo,
            canal = CANAL_TAREAS,
            datos = datos,
            tag = "tarea-$tareaId",
        )
    }

    override fun notificarAlertaRiesgo(
        firebaseUidPsicologo: String,
        pacienteId: Long,
        nombrePaciente: String,
    ) {
        val nombreVisible = nombrePaciente.trim().ifBlank { "un paciente" }
        // El cuerpo es deliberadamente genérico: NUNCA incluimos contenido de las notas en el
        // payload FCM (que pasa por servidores de Google). El psicólogo abre la ficha para ver
        // qué ha detonado el aviso.
        val titulo = "Atención: revisa las notas de $nombreVisible"
        val cuerpo = "Se han detectado posibles indicios de riesgo en sus últimas notas. Revisa la ficha cuanto antes."
        val datos = mapOf(
            CLAVE_TIPO to TIPO_RIESGO,
            CLAVE_PACIENTE_ID to pacienteId.toString(),
            CLAVE_NOMBRE_PACIENTE to nombreVisible,
        )
        enviarATokensDeUsuario(
            firebaseUidDestinatario = firebaseUidPsicologo,
            titulo = titulo,
            cuerpo = cuerpo,
            canal = CANAL_ALERTAS_RIESGO,
            datos = datos,
            // Mismo tag => notificaciones sucesivas se reemplazan en lugar de apilarse.
            tag = "riesgo-$pacienteId",
        )
    }

    private fun enviarATokensDeUsuario(
        firebaseUidDestinatario: String,
        titulo: String,
        cuerpo: String,
        canal: String,
        datos: Map<String, String>,
        tag: String,
    ) {
        if (firebaseApp == null) {
            log.warn(
                "FirebaseApp no inicializado: no se puede enviar push a {}. " +
                    "Comprueba FIREBASE_CREDENTIALS.",
                firebaseUidDestinatario,
            )
            return
        }

        val tokens = servicioFcmToken.obtenerTokensDe(firebaseUidDestinatario)
        if (tokens.isEmpty()) {
            log.info("Sin tokens FCM para uid {} — push omitido", firebaseUidDestinatario)
            return
        }

        val messaging = FirebaseMessaging.getInstance(firebaseApp)
        tokens.forEach { token ->
            val mensaje = Message.builder()
                .setToken(token)
                .putAllData(datos)
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setNotification(
                            AndroidNotification.builder()
                                .setTitle(titulo)
                                .setBody(cuerpo)
                                .setChannelId(canal)
                                .setTag(tag)
                                .build(),
                        )
                        .build(),
                )
                .build()
            try {
                messaging.send(mensaje)
            } catch (e: FirebaseMessagingException) {
                if (esTokenInvalido(e)) {
                    log.info("Token FCM inválido, lo elimino: {}", token.takeLast(8))
                    runCatching { servicioFcmToken.invalidarToken(token) }
                } else {
                    log.warn(
                        "Fallo enviando push a uid {}: code={} msg={}",
                        firebaseUidDestinatario, e.messagingErrorCode, e.message,
                    )
                }
            } catch (e: Exception) {
                log.warn("Error inesperado enviando push a uid {}: {}", firebaseUidDestinatario, e.message)
            }
        }
    }

    private fun esTokenInvalido(e: FirebaseMessagingException): Boolean {
        val codigo = e.messagingErrorCode ?: return false
        return codigo == MessagingErrorCode.UNREGISTERED || codigo == MessagingErrorCode.INVALID_ARGUMENT
    }

    private companion object {
        const val CANAL_CHAT = "chat"
        const val CANAL_TAREAS = "tareas"
        const val CANAL_ALERTAS_RIESGO = "alertas_riesgo"

        const val CLAVE_TIPO = "tipo"
        const val CLAVE_CHAT_ID = "chatId"
        const val CLAVE_PACIENTE_ID = "pacienteId"
        const val CLAVE_PSICOLOGO_ID = "psicologoId"
        const val CLAVE_TAREA_ID = "tareaId"
        const val CLAVE_NOMBRE_PACIENTE = "nombrePaciente"

        const val TIPO_CHAT = "CHAT"
        const val TIPO_TAREA = "TAREA"
        const val TIPO_RIESGO = "RIESGO"
    }
}
