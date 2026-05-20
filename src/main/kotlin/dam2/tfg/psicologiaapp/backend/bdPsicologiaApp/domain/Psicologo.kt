package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*

// Se usa class en lugar de data class para evitar problemas con proxies Hibernate en carga lazy.
@Entity
@Table (name = "PSICOLOGOS")
class Psicologo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    val usuario: Usuario,

    @Column(name = "numero_colegiado", nullable = false, unique = true)
    val numeroColegiado: String,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "psicologo_especialidades",
        joinColumns = [JoinColumn(name = "psicologo_id")]
    )
    @Column(name = "especialidad", nullable = false, length = 80)
    val especialidades: MutableList<String> = mutableListOf(),

    @Column(name = "descripcion", length = 1000)
    var descripcion: String? = null,

    @OneToMany(mappedBy = "psicologo", fetch = FetchType.LAZY)
    val pacientesAsociados: MutableList<Paciente> = mutableListOf(),

    @OneToMany(
        mappedBy = "psicologo",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val notas: MutableList<Nota> = mutableListOf(),

    @OneToMany(
        mappedBy = "psicologo",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    val tareas: MutableList<Tarea> = mutableListOf()

)
