package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PsicologoRepository : JpaRepository<Psicologo, Long> {
    @Query("SELECT p FROM Psicologo p WHERE p.usuario.firebaseUid = :fireBaseId")
    fun findByIdFirebaseUsuario(firebaseUidUsuario: String): Psicologo?
    fun existsByUsuario(usuario: Usuario): Boolean
}