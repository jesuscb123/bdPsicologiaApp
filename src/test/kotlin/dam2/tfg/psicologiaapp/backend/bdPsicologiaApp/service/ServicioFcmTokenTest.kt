package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FcmToken
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.FcmTokenRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

internal class ServicioFcmTokenTest {

    private val fcmTokenRepository: FcmTokenRepository = mock()
    private val usuarioRepository: UsuarioRepository = mock()

    private val servicio = ServicioFcmToken(fcmTokenRepository, usuarioRepository)

    private val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)

    @Test
    fun `registrarToken lanza cuando el token esta vacio`() {
        assertThrows<IllegalArgumentException> {
            servicio.registrarToken("uid1", "   ", null, null)
        }
    }

    @Test
    fun `registrarToken lanza cuando el usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-no")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.registrarToken("uid-no", "token-valido", null, null)
        }
    }

    @Test
    fun `registrarToken crea token nuevo cuando no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(fcmTokenRepository.findByToken("nuevo-token")).thenReturn(null)
        whenever(fcmTokenRepository.save(any<FcmToken>())).thenAnswer { it.getArgument(0) }

        servicio.registrarToken("uid1", "nuevo-token", "inst-1", "android")

        verify(fcmTokenRepository).save(argThat { token == "nuevo-token" && plataforma == "ANDROID" })
    }

    @Test
    fun `registrarToken actualiza token existente y limpia instalacion previa`() {
        val existente = FcmToken(usuario = usuario, token = "token-existente", instalacionId = "vieja")
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(fcmTokenRepository.findByToken("token-existente")).thenReturn(existente)
        whenever(fcmTokenRepository.save(existente)).thenReturn(existente)
        whenever(
            fcmTokenRepository.deleteOtrosDeMismaInstalacion(
                usuarioId = 1L,
                instalacionId = "inst-1",
                tokenAExcluir = "token-existente",
            ),
        ).thenReturn(2)

        servicio.registrarToken("uid1", "token-existente", "inst-1", null)

        verify(fcmTokenRepository).save(existente)
        verify(fcmTokenRepository).deleteOtrosDeMismaInstalacion(1L, "inst-1", "token-existente")
    }

    @Test
    fun `eliminarToken no borra cuando el token pertenece a otro usuario`() {
        val otroUsuario = Usuario(2L, "uid-otro", "otro@b.com", "Otro", "User", null)
        val existente = FcmToken(usuario = otroUsuario, token = "token-ajeno")
        whenever(fcmTokenRepository.findByToken("token-ajeno")).thenReturn(existente)

        servicio.eliminarToken("uid1", "token-ajeno")

        verify(fcmTokenRepository, never()).deleteByToken(any())
    }

    @Test
    fun `eliminarToken borra cuando el token es del usuario`() {
        val existente = FcmToken(usuario = usuario, token = "mi-token")
        whenever(fcmTokenRepository.findByToken("mi-token")).thenReturn(existente)

        servicio.eliminarToken("uid1", "mi-token")

        verify(fcmTokenRepository).deleteByToken("mi-token")
    }

    @Test
    fun `obtenerTokensDe devuelve lista de tokens del usuario`() {
        val token1 = FcmToken(usuario = usuario, token = "t1")
        val token2 = FcmToken(usuario = usuario, token = "t2")
        whenever(fcmTokenRepository.findAllByUsuarioFirebaseUid("uid1")).thenReturn(listOf(token1, token2))

        val resultado = servicio.obtenerTokensDe("uid1")

        assertEquals(listOf("t1", "t2"), resultado)
    }

    @Test
    fun `invalidarToken elimina token cuando existe`() {
        servicio.invalidarToken("token-a-borrar")

        verify(fcmTokenRepository).deleteByToken("token-a-borrar")
    }
}
