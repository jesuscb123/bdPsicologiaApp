package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*
import java.time.LocalDateTime

// Se usa class en lugar de data class para evitar problemas con proxies Hibernate en carga lazy.
@Entity
@Table (name = "TAREAS")
class Tarea(
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

    /** El paciente ha aceptado la tarea en la app; obligatorio antes de poder marcarla como completada. */
    @Column(name = "aceptada_por_paciente", nullable = false)
    var aceptadaPorPaciente: Boolean = false,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "psicologo_id", nullable = false)
    var psicologo: Psicologo,

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "paciente_id", nullable = false)
    var paciente: Paciente,

    @Column(name = "ultima_modificacion", nullable = false)
    var ultimaModificacion: LocalDateTime = LocalDateTime.now(),
) {
    @PrePersist
    fun alCrear() {
        ultimaModificacion = LocalDateTime.now()
    }

    @PreUpdate
    fun alActualizar() {
        ultimaModificacion = LocalDateTime.now()
    }
}
