package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table (name = "TAREAS")
data class Tarea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "titulo_tarea", nullable = false)
    var tituloTarea: String,

    @Column(name = "descripcion_tarea", nullable = false)
    var descripcionTarea: String,

    @Column(name = "hora_envio", nullable = false)
    var horaEnvio: LocalDateTime = LocalDateTime.now(),

    @Column(name = "realizada", nullable = false)
    var realizada: Boolean = false,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "psicologo_id", nullable = false)
    var psicologo: Psicologo,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "paciente_id", nullable = false)
    var paciente: Paciente
)
