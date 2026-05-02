package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioChat
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO.ChatResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val servicioChat: IServicioChat
) {

    @PostMapping("/me/psicologo")
    @PreAuthorize("hasRole('PACIENTE')")
    fun asegurarChatConPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<Any> {
        return try {
            val respuesta = servicioChat.asegurarChatPaciente(usuarioFirebase.uid)
            ResponseEntity.ok(respuesta)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @PostMapping("/pacientes/{pacienteId}")
    @PreAuthorize("hasRole('PSICOLOGO')")
    fun asegurarChatConPaciente(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<Any> {
        return try {
            val respuesta = servicioChat.asegurarChatPsicologo(usuarioFirebase.uid, pacienteId)
            ResponseEntity.ok(respuesta)
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: SecurityException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.message)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }
}
