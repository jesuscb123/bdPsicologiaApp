package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*

@Entity
@Table(name = "psicologos")
class Psicologo(
    id: Long? = null,
    nombreUsuario: String,

    @Column(nullable = false)
    var especialidad: String
) : Usuario(id, nombreUsuario){

    @OneToMany(
        mappedBy = "psicologo",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    var pacientes: MutableList<Paciente> = mutableListOf()

    @OneToMany(
        mappedBy = "psicologo",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL]
    )
    var tareas: MutableList<Tarea> = mutableListOf()
}
