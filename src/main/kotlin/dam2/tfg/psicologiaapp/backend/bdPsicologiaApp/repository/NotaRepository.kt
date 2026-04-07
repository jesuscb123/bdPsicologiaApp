package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface NotaRepository : JpaRepository<Nota, Long> {
    interface EstadoNotasProjection {
        val ultimaModificacion: LocalDateTime?
        val total: Long
    }

    @Query("SELECT n FROM Nota n WHERE n.paciente.id = :pacienteId AND n.psicologo.id = :psicologoId")
    fun obtenerNotasPacienteParaPsicologo(
        @Param("pacienteId") pacienteId: Long,
        @Param("psicologoId") psicologoId: Long
    ): List<Nota>

    @Query("SELECT n FROM Nota n WHERE n.paciente.usuario.firebaseUid = :fireBaseId")
    fun obtenerNotasByPacienteUsuarioFirebaseId(@Param("fireBaseId") fireBaseId: String): List<Nota>

    @Query(
        """
        SELECT MAX(n.ultimaModificacion) AS ultimaModificacion, COUNT(n) AS total
        FROM Nota n
        WHERE n.paciente.usuario.firebaseUid = :firebaseUidPaciente
        """
    )
    fun obtenerEstadoNotasPaciente(
        @Param("firebaseUidPaciente") firebaseUidPaciente: String
    ): EstadoNotasProjection

    @Query(
        """
        SELECT MAX(n.ultimaModificacion) AS ultimaModificacion, COUNT(n) AS total
        FROM Nota n
        WHERE n.paciente.id = :pacienteId
          AND n.psicologo.id = :psicologoId
        """
    )
    fun obtenerEstadoNotasPacienteParaPsicologo(
        @Param("pacienteId") pacienteId: Long,
        @Param("psicologoId") psicologoId: Long
    ): EstadoNotasProjection
}