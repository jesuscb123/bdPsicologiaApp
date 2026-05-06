package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notificacionesDTO

import jakarta.validation.constraints.NotBlank

/**
 * Petición que el cliente envía tras escribir un mensaje en Firebase Realtime Database.
 * El backend valida que el remitente pertenece al chat y manda el push al destinatario.
 *
 * No transportamos datos sensibles innecesarios: el [vistaPreviaTexto] se trunca en el
 * cliente y solo se usa para mostrar el adelanto en la notificación.
 */
data class NotificarMensajeChatRequest(
    @field:NotBlank
    val chatId: String,
    @field:NotBlank
    val vistaPreviaTexto: String,
)
