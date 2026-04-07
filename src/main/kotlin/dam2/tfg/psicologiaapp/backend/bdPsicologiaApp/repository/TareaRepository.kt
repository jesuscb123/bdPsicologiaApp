package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface TareaRepository : JpaRepository<Tarea, Long> {
    interface EstadoTareasProjection {
        val ultimaModificacion: LocalDateTime?
        val total: Long
    }

    @Query(
        """
        SELECT t FROM Tarea t
        WHERE t.paciente.usuario.firebaseUid = :firebaseUidPaciente
        ORDER BY t.horaEnvio DESC
        """
    )
    fun findTareasByPacienteFirebaseUid(
        @Param("firebaseUidPaciente") firebaseUidPaciente: String
    ): List<Tarea>

    @Query(
        """
        SELECT t FROM Tarea t
        WHERE t.psicologo.usuario.firebaseUid = :firebaseUidPsicologo
          AND t.paciente.id = :pacienteId
        ORDER BY t.horaEnvio DESC
        """
    )
    fun findTareasByPsicologoFirebaseUidAndPacienteId(
        @Param("firebaseUidPsicologo") firebaseUidPsicologo: String,
        @Param("pacienteId") pacienteId: Long
    ): List<Tarea>

    @Query(
        """
        SELECT MAX(t.ultimaModificacion) AS ultimaModificacion, COUNT(t) AS total
        FROM Tarea t
        WHERE t.paciente.usuario.firebaseUid = :firebaseUidPaciente
        """
    )
    fun obtenerEstadoTareasPaciente(
        @Param("firebaseUidPaciente") firebaseUidPaciente: String
    ): EstadoTareasProjection

    @Query(
        """
        SELECT MAX(t.ultimaModificacion) AS ultimaModificacion, COUNT(t) AS total
        FROM Tarea t
        WHERE t.psicologo.usuario.firebaseUid = :firebaseUidPsicologo
          AND t.paciente.id = :pacienteId
        """
    )
    fun obtenerEstadoTareasPacienteParaPsicologo(
        @Param("firebaseUidPsicologo") firebaseUidPsicologo: String,
        @Param("pacienteId") pacienteId: Long
    ): EstadoTareasProjection
}