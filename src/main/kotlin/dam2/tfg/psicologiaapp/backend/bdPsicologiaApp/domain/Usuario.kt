package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Usuario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var nombreUsuario: String,
)