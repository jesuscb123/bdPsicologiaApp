package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notificacionesDTO

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Petición que el cliente envía tras escribir un mensaje en Firebase Realtime Database.
 * El backend valida que el remitente pertenece al chat y manda el push al destinatario.
 *
 * No transportamos datos sensibles innecesarios: el [vistaPreviaTexto] se trunca en el
 * cliente y solo se usa para mostrar el adelanto en la notificación.
 */
data class NotificarMensajeChatRequest(
    @field:NotBlank
    @field:Size(max = 200, message = "El chatId no puede superar 200 caracteres")
    val chatId: String,

    @field:NotBlank
    @field:Size(max = 200, message = "La vista previa no puede superar 200 caracteres")
    val vistaPreviaTexto: String,
)
