package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repositories

import jakarta.persistence.*

@Entity
@Table (name = "NOTAS")
class Nota(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column (nullable = false)
    var asunto: String = "",

    @Column (nullable = false)
    var descripcion: String = "",

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "paciente_id", nullable = false)
    var paciente: Paciente,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id", nullable = false)
    var psicologo: Psicologo
)
