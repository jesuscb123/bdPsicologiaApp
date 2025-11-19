package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repositories

import jakarta.persistence.*

@Entity
@Table (name = "PACIENTES")
data class Paciente(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val usuario: Usuario,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id", nullable = false, unique = true)
    var psicologo: Psicologo,

    @OneToMany(
        mappedBy = "paciente",
        fetch = FetchType.LAZY
    )
    val notas: MutableList<Nota> = mutableListOf(),

    @OneToMany(
        mappedBy = "paciente",
        fetch = FetchType.LAZY
    )
    val tareas: MutableList<Tarea> = mutableListOf()

)
