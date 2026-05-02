package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO.ChatResponse

interface IServicioChat {
    fun asegurarChatPaciente(firebaseUidPaciente: String): ChatResponse
    fun asegurarChatPsicologo(firebaseUidPsicologo: String, pacienteId: Long): ChatResponse
}
