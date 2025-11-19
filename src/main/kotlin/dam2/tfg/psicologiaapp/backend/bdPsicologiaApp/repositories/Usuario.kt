package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repositories

import jakarta.persistence.*

@Entity
@Table (name = "USUARIOS")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "firebase_uid", nullable = false, unique = true)
    val firebaseUid: String,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column (name = "nombreUsuario", nullable = false, unique = true)
     var nombreUsuario: String,

    @Column (name = "es_psicologo", nullable = false)
    var esPsicologo: Boolean = false,

    @Column (name = "es_paciente", nullable = false)
    var esPaciente: Boolean = false
) {
}