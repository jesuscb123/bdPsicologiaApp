package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*
import javax.management.monitor.StringMonitor

@Entity
@Table (name = "tareas")
data class Tarea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "titulo_tarea", nullable = false)
    var tituloTarea: String,

    @Column(name = "descripcion_tarea", nullable = false)
    var descripcionTarea: String,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "psicologo_id", nullable = false)
    var psicologo: Psicologo,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "paciente_id", nullable = false)
    var paciente: Paciente
)
