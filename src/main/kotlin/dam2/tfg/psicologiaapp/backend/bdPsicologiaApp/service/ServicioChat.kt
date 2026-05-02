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
    private val psicologoRepository: PsicologoRepository
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
