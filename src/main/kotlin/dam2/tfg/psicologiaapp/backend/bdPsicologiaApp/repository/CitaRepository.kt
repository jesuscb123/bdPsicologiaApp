package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Cita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface CitaRepository : JpaRepository<Cita, Long> {

    @Query(
        """
        SELECT c FROM Cita c
        WHERE c.paciente.usuario.firebaseUid = :firebaseUidPaciente
        ORDER BY c.inicio DESC
        """
    )
    fun findCitasByPacienteFirebaseUid(
        @Param("firebaseUidPaciente") firebaseUidPaciente: String
    ): List<Cita>

    @Query(
        """
        SELECT c FROM Cita c
        WHERE c.psicologo.usuario.firebaseUid = :firebaseUidPsicologo
        ORDER BY c.inicio DESC
        """
    )
    fun findCitasByPsicologoFirebaseUid(
        @Param("firebaseUidPsicologo") firebaseUidPsicologo: String
    ): List<Cita>

    @Query(
        """
        SELECT c FROM Cita c
        WHERE c.psicologo.id = :psicologoId
          AND c.inicio >= :inicioDesde
          AND c.inicio < :inicioHasta
          AND c.estado = :estado
        """
    )
    fun findByPsicologoIdAndInicioEnRangoAndEstado(
        @Param("psicologoId") psicologoId: Long,
        @Param("inicioDesde") inicioDesde: Instant,
        @Param("inicioHasta") inicioHasta: Instant,
        @Param("estado") estado: EstadoCita = EstadoCita.RESERVADA
    ): List<Cita>

    /**
     * Citas cuyo inicio cae en [inicioDesde, inicioHasta) (mitad abierta por hora/slot).
     * Útil cuando la igualdad exacta de [Instant] falla por precisión o capa de persistencia.
     */
    @Query(
        """
        SELECT c FROM Cita c
        WHERE c.psicologo.id = :psicologoId
          AND c.inicio >= :inicioDesde
          AND c.inicio < :inicioHasta
        """
    )
    fun findByPsicologoIdAndInicioEnRango(
        @Param("psicologoId") psicologoId: Long,
        @Param("inicioDesde") inicioDesde: Instant,
        @Param("inicioHasta") inicioHasta: Instant,
    ): List<Cita>
}

