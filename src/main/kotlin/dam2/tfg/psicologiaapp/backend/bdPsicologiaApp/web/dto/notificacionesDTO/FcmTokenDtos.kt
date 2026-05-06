package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notificacionesDTO

import jakarta.validation.constraints.NotBlank

/**
 * Petición que envía el cliente para registrar (o renovar) el token FCM
 * asociado al usuario autenticado.
 */
data class RegistrarFcmTokenRequest(
    @field:NotBlank
    val token: String,
    /** Identificador estable de la instalación (Firebase Installations) que produjo este token. */
    val instalacionId: String? = null,
    /** Plataforma del cliente (ANDROID, IOS, WEB). Por defecto ANDROID. */
    val plataforma: String? = null,
)

/** Petición que envía el cliente al cerrar sesión para borrar su token actual. */
data class EliminarFcmTokenRequest(
    @field:NotBlank
    val token: String,
)
