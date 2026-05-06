package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FcmToken
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FcmTokenRepository : JpaRepository<FcmToken, Long> {

    fun findByToken(token: String): FcmToken?

    fun findAllByUsuario(usuario: Usuario): List<FcmToken>

    @Query(
        "SELECT t FROM FcmToken t WHERE t.usuario.firebaseUid = :firebaseUid",
    )
    fun findAllByUsuarioFirebaseUid(@Param("firebaseUid") firebaseUid: String): List<FcmToken>

    @Modifying
    @Query("DELETE FROM FcmToken t WHERE t.token = :token")
    fun deleteByToken(@Param("token") token: String): Int

    @Modifying
    @Query(
        "DELETE FROM FcmToken t " +
            "WHERE t.instalacionId = :instalacionId AND t.usuario.id = :usuarioId AND t.token <> :tokenAExcluir",
    )
    fun deleteOtrosDeMismaInstalacion(
        @Param("usuarioId") usuarioId: Long,
        @Param("instalacionId") instalacionId: String,
        @Param("tokenAExcluir") tokenAExcluir: String,
    ): Int
}
