package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*

@Entity
@Table (name = "PSICOLOGOS")
data class Psicologo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    val user: Usuario,

    @Column(name = "numero_colegiado", nullable = false, unique = true)
    val numeroColegioado: String = "",

    @Column(name = "especialidad", nullable = false)
    val especialidad: String = "",

    @OneToMany(mappedBy = "psicologo", fetch = FetchType.LAZY)
    val pacientesAsociados: MutableList<Paciente> = mutableListOf(),

    @OneToMany(mappedBy = "psicologo", fetch = FetchType.LAZY)
    val notas: MutableList<Nota> = mutableListOf(),

    @OneToMany(
        mappedBy = "psicologo",
        fetch = FetchType.LAZY
    )
    val tareas: MutableList<Tarea> = mutableListOf()
)
