package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PacienteRepository : JpaRepository<Paciente, Long> {

    @Query("SELECT p FROM Paciente p WHERE p.usuario.firebaseUid = :firebaseUidUsuario")
    fun findByIdFirebaseUsuario(@Param("firebaseUidUsuario") firebaseUidUsuario: String): Paciente?

    fun existsByUsuario(usuario: Usuario): Boolean

    fun findAllByPsicologo(psicologo: Psicologo): List<Paciente>

    fun findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase(
        nombre: String,
        apellidos: String
    ): List<Paciente>

    /**
     * Carga un paciente trayendo en la misma query su usuario y el psicólogo asignado con su
     * usuario. Lo usa la evaluación asíncrona de riesgo: como corre en otro hilo y posiblemente
     * fuera del scope de la transacción que disparó el evento, necesitamos materializar todo lo
     * que vamos a leer (nombre del paciente y firebaseUid del psicólogo) en un solo viaje.
     */
    @Query(
        """
        SELECT p FROM Paciente p
        JOIN FETCH p.usuario
        LEFT JOIN FETCH p.psicologo psi
        LEFT JOIN FETCH psi.usuario
        WHERE p.id = :pacienteId
        """
    )
    fun findByIdConPsicologoYUsuarios(@Param("pacienteId") pacienteId: Long): Paciente?
}