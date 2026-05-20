package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.LocalDateTime

/**
 * Token FCM (Firebase Cloud Messaging) registrado por una instalación concreta de la app
 * para enviarle notificaciones push al usuario propietario.
 *
 * Un mismo usuario puede tener varios tokens (móvil + tablet, reinstalación, etc.) y un mismo
 * token NUNCA debe pertenecer a dos usuarios distintos: la columna [token] es única para evitar
 * mandar mensajes destinados a la persona equivocada cuando alguien cierra sesión y otra inicia
 * en el mismo dispositivo.
 */
@Entity
@Table(
    name = "FCM_TOKENS",
    indexes = [
        Index(name = "idx_fcm_token_usuario", columnList = "usuario_id"),
    ],
)
// Se usa class en lugar de data class para evitar problemas con proxies Hibernate en carga lazy.
class FcmToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario,

    @Column(name = "token", nullable = false, unique = true, length = 4096)
    var token: String,

    /**
     * Identificador estable de la instalación devuelto por Firebase Installations en el cliente.
     * Permite limpiar todos los tokens viejos del mismo dispositivo cuando llega uno nuevo.
     */
    @Column(name = "instalacion_id", length = 256)
    var instalacionId: String? = null,

    @Column(name = "plataforma", nullable = false, length = 32)
    var plataforma: String = "ANDROID",

    @Column(name = "creado_en", nullable = false)
    var creadoEn: LocalDateTime = LocalDateTime.now(),

    @Column(name = "actualizado_en", nullable = false)
    var actualizadoEn: LocalDateTime = LocalDateTime.now(),
) {
    @PrePersist
    fun alCrear() {
        val ahora = LocalDateTime.now()
        creadoEn = ahora
        actualizadoEn = ahora
    }

    @PreUpdate
    fun alActualizar() {
        actualizadoEn = LocalDateTime.now()
    }
}
