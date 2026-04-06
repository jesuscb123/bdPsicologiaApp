package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TareaRepository : JpaRepository<Tarea, Long> {
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

    /** Tareas creadas antes de [aceptadaPorPaciente]: si ya estaban completadas, se consideran aceptadas. */
    @Modifying(clearAutomatically = true)
    @Query(
        "UPDATE Tarea t SET t.aceptadaPorPaciente = true WHERE t.realizada = true AND t.aceptadaPorPaciente = false"
    )
    fun marcarAceptadasParaTareasYaRealizadas(): Int
}