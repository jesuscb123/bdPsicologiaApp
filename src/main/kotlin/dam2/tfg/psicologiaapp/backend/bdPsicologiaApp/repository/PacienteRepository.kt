package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface PacienteRepository : JpaRepository<Paciente, Long> {
    fun findByIdFirebaseUsuario(firebaseUidUsuario: String): Paciente?
    fun existeUsuario(usuario: Usuario): Boolean
}