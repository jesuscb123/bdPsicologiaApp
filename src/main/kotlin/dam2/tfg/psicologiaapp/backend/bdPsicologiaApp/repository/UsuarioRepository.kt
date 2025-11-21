package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UsuarioRepository : JpaRepository<Usuario, Long> {
    @Query("SELECT u FROM Usuario u WHERE u.firebaseUid = :idFirebase")
    fun findByFirebaseUid(idFirebase: String) : Usuario?
}