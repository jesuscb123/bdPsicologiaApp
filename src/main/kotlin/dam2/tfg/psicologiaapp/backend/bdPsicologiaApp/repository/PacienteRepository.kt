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

    fun findByUsuarioNombreUsuarioContainingIgnoreCase(nombreUsuario: String): List<Paciente>
}