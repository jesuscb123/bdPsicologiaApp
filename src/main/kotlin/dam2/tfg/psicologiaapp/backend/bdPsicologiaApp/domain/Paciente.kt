package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*

@Entity
@Table (name = "pacientes")
class Paciente(
    id: Long? = null,
    nombreUsurio: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id")
    var psicologo: Psicologo? = null

): Usuario(id, nombreUsurio) {

    @OneToMany(
        mappedBy = "paciente",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL]
    )
    var listaTareas: MutableList<Tarea> = mutableListOf()

    @OneToMany(
        mappedBy = "paciente",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL]
    )
    var listaNotas: MutableList<Nota> = mutableListOf()


}