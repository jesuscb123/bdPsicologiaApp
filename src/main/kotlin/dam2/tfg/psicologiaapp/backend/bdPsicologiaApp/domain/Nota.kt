package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain

import jakarta.persistence.*
import java.time.LocalDateTime

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
    var psicologo: Psicologo,

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
