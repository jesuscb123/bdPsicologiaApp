package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository

interface PsicologoRepository : JpaRepository<Psicologo, Long> {
    fun findByIdFirebaseUsuario(firebaseUidUsuario: String): Psicologo?
    fun existeUsuario(usuario: Usuario): Boolean
}