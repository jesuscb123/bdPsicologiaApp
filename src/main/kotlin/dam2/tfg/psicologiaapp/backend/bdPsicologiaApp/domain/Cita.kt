package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(
    name = "CITAS",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_citas_psicologo_inicio", columnNames = ["psicologo_id", "inicio"])
    ]
)
class Cita(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psicologo_id", nullable = false)
    var psicologo: Psicologo,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    var paciente: Paciente,

    @Column(name = "inicio", nullable = false)
    var inicio: Instant,

    @Column(name = "duracion_minutos", nullable = false)
    var duracionMinutos: Int = 60,

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    var estado: EstadoCita = EstadoCita.RESERVADA,

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

