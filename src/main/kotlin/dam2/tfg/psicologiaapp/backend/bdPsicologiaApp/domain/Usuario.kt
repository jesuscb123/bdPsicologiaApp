package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table (name = "USUARIOS")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "firebase_uid", nullable = false, unique = true)
    val firebaseUid: String,

    @Column(name = "email", nullable = false, unique = true)
    var email: String,

    @Column (name = "nombreUsuario", nullable = false, unique = true)
     var nombreUsuario: String,

    /** Sin @Lob: en PostgreSQL + lazy loading provocaba "Unable to access lob stream". */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "foto_perfil")
    var fotoPerfilUrl: String? = null

) {
}