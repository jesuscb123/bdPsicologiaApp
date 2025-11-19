package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import java.util.LongSummaryStatistics

interface UsuarioRepository : JpaRepository<Usuario, LongSummaryStatistics> {
    fun encontrarByFirebaseUid(idFirebase: String) : Usuario?
}