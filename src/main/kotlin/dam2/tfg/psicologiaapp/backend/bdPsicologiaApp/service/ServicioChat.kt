package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.database.FirebaseDatabase
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO.ChatResponse
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@Service
class ServicioChat(
    private val pacienteRepository: PacienteRepository,
    private val psicologoRepository: PsicologoRepository,
    private val servicioNotificacionesPush: IServicioNotificacionesPush,
) : IServicioChat {

    private val log = LoggerFactory.getLogger(ServicioChat::class.java)

    @Transactional(readOnly = true)
    override fun asegurarChatPaciente(firebaseUidPaciente: String): ChatResponse {
        val paciente = pacienteRepository.findByIdFirebaseUsuario(firebaseUidPaciente)
            ?: throw IllegalStateException("Paciente no encontrado para el uid: $firebaseUidPaciente")

        val psicologo = paciente.psicologo
            ?: throw IllegalStateException("El paciente no tiene psicólogo asignado")

        val idPaciente = paciente.id ?: throw IllegalStateException("ID de paciente nulo")
        val idPsicologo = psicologo.id ?: throw IllegalStateException("ID de psicólogo nulo")
        val chatId = generarChatId(idPaciente, idPsicologo)
        val uidPaciente = paciente.usuario.firebaseUid
        val uidPsicologo = psicologo.usuario.firebaseUid
        val nombreInterlocutor = psicologo.usuario.nombre
        val apellidosInterlocutor = psicologo.usuario.apellidos
        val fotoInterlocutor = psicologo.usuario.fotoPerfilUrl

        asegurarNodoRtdb(chatId, uidPaciente, uidPsicologo, idPaciente, idPsicologo)

        return ChatResponse(
            chatId = chatId,
            interlocutorNombre = nombreInterlocutor,
            interlocutorApellidos = apellidosInterlocutor,
            interlocutorFotoPerfilUrl = fotoInterlocutor,
            rtdbRuta = "chats/$chatId"
        )
    }

    @Transactional(readOnly = true)
    override fun asegurarChatPsicologo(firebaseUidPsicologo: String, pacienteId: Long): ChatResponse {
        val psicologo = psicologoRepository.findByIdFirebaseUsuario(firebaseUidPsicologo)
            ?: throw IllegalStateException("Psicólogo no encontrado para el uid: $firebaseUidPsicologo")

        val paciente = pacienteRepository.findByIdOrNull(pacienteId)
            ?: throw IllegalStateException("Paciente no encontrado con id: $pacienteId")

        if (paciente.psicologo?.id != psicologo.id) {
            throw SecurityException("El paciente con id $pacienteId no está asignado a este psicólogo")
        }

        val idPaciente = paciente.id ?: throw IllegalStateException("ID de paciente nulo")
        val idPsicologo = psicologo.id ?: throw IllegalStateException("ID de psicólogo nulo")
        val chatId = generarChatId(idPaciente, idPsicologo)
        val uidPaciente = paciente.usuario.firebaseUid
        val uidPsicologo = psicologo.usuario.firebaseUid
        val nombreInterlocutor = paciente.usuario.nombre
        val apellidosInterlocutor = paciente.usuario.apellidos
        val fotoInterlocutor = paciente.usuario.fotoPerfilUrl

        asegurarNodoRtdb(chatId, uidPaciente, uidPsicologo, idPaciente, idPsicologo)

        return ChatResponse(
            chatId = chatId,
            interlocutorNombre = nombreInterlocutor,
            interlocutorApellidos = apellidosInterlocutor,
            interlocutorFotoPerfilUrl = fotoInterlocutor,
            rtdbRuta = "chats/$chatId"
        )
    }

    @Transactional(readOnly = true)
    override fun notificarMensajeChat(
        firebaseUidRemitente: String,
        chatId: String,
        vistaPreviaTexto: String,
    ) {
        if (chatId.isBlank()) throw IllegalArgumentException("chatId vacío")

        val (pacienteId, psicologoId) = parsearChatId(chatId)
            ?: throw IllegalArgumentException("Formato de chatId inválido: $chatId")

        val paciente = pacienteRepository.findByIdOrNull(pacienteId)
            ?: throw IllegalStateException("Paciente $pacienteId no encontrado")
        val psicologoDelPaciente = paciente.psicologo
            ?: throw IllegalStateException("El paciente no tiene psicólogo asignado")

        if (psicologoDelPaciente.id != psicologoId) {
            throw SecurityException("La asignación paciente-psicólogo no coincide con el chat")
        }

        val uidPaciente = paciente.usuario.firebaseUid
        val uidPsicologo = psicologoDelPaciente.usuario.firebaseUid

        val (uidDestinatario, nombreRemitente) = when (firebaseUidRemitente) {
            uidPaciente -> uidPsicologo to nombreCompleto(paciente.usuario.nombre, paciente.usuario.apellidos)
            uidPsicologo -> uidPaciente to nombreCompleto(psicologoDelPaciente.usuario.nombre, psicologoDelPaciente.usuario.apellidos)
            else -> throw SecurityException("El usuario autenticado no participa en el chat $chatId")
        }

        val previewTruncada = vistaPreviaTexto.trim().take(140)
        try {
            servicioNotificacionesPush.notificarNuevoMensajeChat(
                firebaseUidDestinatario = uidDestinatario,
                chatId = chatId,
                nombreRemitente = nombreRemitente,
                vistaPreviaTexto = previewTruncada,
                pacienteId = pacienteId,
                psicologoId = psicologoId,
            )
        } catch (e: Exception) {
            log.warn("Fallo enviando push de chat {}: {}", chatId, e.message)
        }
    }

    private fun parsearChatId(chatId: String): Pair<Long, Long>? {
        // El formato es "paciente_{pacienteId}_psicologo_{psicologoId}".
        val regex = Regex("^paciente_(\\d+)_psicologo_(\\d+)$")
        val match = regex.matchEntire(chatId) ?: return null
        val pacienteId = match.groupValues[1].toLongOrNull() ?: return null
        val psicologoId = match.groupValues[2].toLongOrNull() ?: return null
        return pacienteId to psicologoId
    }

    private fun nombreCompleto(nombre: String?, apellidos: String?): String {
        val partes = listOfNotNull(nombre?.takeIf { it.isNotBlank() }, apellidos?.takeIf { it.isNotBlank() })
        return partes.joinToString(" ").ifBlank { "Mensaje nuevo" }
    }

    private fun generarChatId(pacienteId: Long, psicologoId: Long): String =
        "paciente_${pacienteId}_psicologo_${psicologoId}"

    private fun asegurarNodoRtdb(
        chatId: String,
        uidPaciente: String,
        uidPsicologo: String,
        pacienteId: Long,
        psicologoId: Long
    ) {
        try {
            val datosChatNodo = mapOf(
                "pacienteId" to pacienteId,
                "psicologoId" to psicologoId,
                "pacienteUid" to uidPaciente,
                "psicologoUid" to uidPsicologo,
                "participantes" to mapOf(
                    uidPaciente to true,
                    uidPsicologo to true
                )
            )
            FirebaseDatabase.getInstance()
                .getReference("chats/$chatId")
                .updateChildrenAsync(datosChatNodo)
                .get(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            log.error("Error al asegurar el nodo RTDB para chatId {}: {}", chatId, e.message, e)
            throw IllegalStateException("No se pudo crear o asegurar el chat en la base de datos en tiempo real", e)
        }
    }
}
