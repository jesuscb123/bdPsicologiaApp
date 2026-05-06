package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioFcmToken
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notificacionesDTO.EliminarFcmTokenRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notificacionesDTO.RegistrarFcmTokenRequest
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints para que cualquier usuario autenticado registre o dé de baja sus tokens FCM.
 * No tiene autorización por rol porque tanto pacientes como psicólogos los necesitan.
 */
@RestController
@RequestMapping("/api/notificaciones")
class NotificacionesController(
    private val servicioFcmToken: IServicioFcmToken,
) {

    private val log = LoggerFactory.getLogger(NotificacionesController::class.java)

    @PostMapping("/fcm/token")
    fun registrarToken(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: RegistrarFcmTokenRequest,
    ): ResponseEntity<Any> {
        return try {
            servicioFcmToken.registrarToken(
                firebaseUidUsuario = usuarioFirebase.uid,
                token = request.token,
                instalacionId = request.instalacionId,
                plataforma = request.plataforma,
            )
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(e.message)
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
        } catch (e: Exception) {
            log.error("Error registrando token FCM para uid {}: {}", usuarioFirebase.uid, e.message, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }

    @PostMapping("/fcm/token/baja")
    fun eliminarToken(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @Valid @RequestBody request: EliminarFcmTokenRequest,
    ): ResponseEntity<Any> {
        return try {
            servicioFcmToken.eliminarToken(usuarioFirebase.uid, request.token)
            ResponseEntity.noContent().build()
        } catch (e: Exception) {
            log.warn("Error dando de baja token FCM: {}", e.message)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: ${e.message}")
        }
    }
}
