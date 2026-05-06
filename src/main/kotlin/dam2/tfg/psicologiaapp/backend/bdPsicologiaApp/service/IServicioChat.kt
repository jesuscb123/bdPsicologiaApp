package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO.ChatResponse

interface IServicioChat {
    fun asegurarChatPaciente(firebaseUidPaciente: String): ChatResponse
    fun asegurarChatPsicologo(firebaseUidPsicologo: String, pacienteId: Long): ChatResponse

    /**
     * Envía un push al destinatario del chat [chatId]. El remitente se identifica por el uid
     * autenticado y se valida que pertenece al chat. La vista previa se trunca por el cliente
     * y aquí solo se reenvía al SDK FCM.
     */
    fun notificarMensajeChat(
        firebaseUidRemitente: String,
        chatId: String,
        vistaPreviaTexto: String,
    )
}
