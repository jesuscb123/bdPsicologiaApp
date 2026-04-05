package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

internal class ServicioUsuarioTest {

    private val usuarioRepository: UsuarioRepository = mock()
    private val psicologoRepository: PsicologoRepository = mock()
    private val pacienteRepository: PacienteRepository = mock()
    private val servicioPsicologo: IServicioPsicologo = mock()
    private val servicioPaciente: IServicioPaciente = mock()
    private val servicioAlmacenamientoFotoPerfil: ServicioAlmacenamientoFotoPerfil = mock()

    private val servicio = ServicioUsuario(
        usuarioRepository,
        psicologoRepository,
        pacienteRepository,
        servicioPsicologo,
        servicioPaciente,
        servicioAlmacenamientoFotoPerfil,
    )

    @Test
    fun `obtenerUsuarioByFireBaseId devuelve null cuando no existe el usuario`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-inexistente")).thenReturn(null)

        val resultado = servicio.obtenerUsuarioByFireBaseId("uid-inexistente")

        assertNull(resultado)
    }

    @Test
    fun `obtenerUsuarioByFireBaseId devuelve response cuando existe el usuario`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "nombreUser", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        val resultado = servicio.obtenerUsuarioByFireBaseId("uid1")

        assertNotNull(resultado)
        assertEquals(1L, resultado!!.id)
        assertEquals("uid1", resultado.firebaseUid)
        assertEquals("nombreUser", resultado.nombreUsuario)
    }

    @Test
    fun `obtenerPerfilUsuario lanza cuando el usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-no-existe")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.obtenerPerfilUsuario("uid-no-existe")
        }
    }

    @Test
    fun `obtenerPerfilUsuario devuelve perfil basico cuando no es psicologo ni paciente`() {
        val usuario = Usuario(1L, "uid1", "email@test.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)

        val resultado = servicio.obtenerPerfilUsuario("uid1")

        assertEquals(1L, resultado.id)
        assertEquals("uid1", resultado.firebaseUid)
        assertEquals("email@test.com", resultado.email)
        assertEquals("SIN_ROL", resultado.rol)
    }

    @Test
    fun `actualizarEmailUsuario lanza cuando el usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-no")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.actualizarEmailUsuario("uid-no", "nuevo@email.com")
        }
    }

    @Test
    fun `actualizarEmailUsuario lanza cuando el email tiene formato invalido`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        assertThrows<IllegalArgumentException> {
            servicio.actualizarEmailUsuario("uid1", "email-invalido")
        }
    }

    @Test
    fun `actualizarEmailUsuario lanza cuando el email ya esta en uso`() {
        val usuario = Usuario(1L, "uid1", "viejo@b.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(usuarioRepository.existsByEmail("otro@b.com")).thenReturn(true)

        assertThrows<IllegalStateException> {
            servicio.actualizarEmailUsuario("uid1", "otro@b.com")
        }
    }

    @Test
    fun `actualizarFotoPerfilUsuario lanza cuando el usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-no")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.actualizarFotoPerfilUsuario("uid-no", "https://example.com/a.jpg")
        }
    }

    @Test
    fun `actualizarFotoPerfilUsuario lanza cuando la url es invalida`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        assertThrows<IllegalArgumentException> {
            servicio.actualizarFotoPerfilUsuario("uid1", "no-es-una-url")
        }
    }

    @Test
    fun `actualizarFotoPerfilUsuario persiste y devuelve perfil`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)

        val resultado = servicio.actualizarFotoPerfilUsuario("uid1", "  https://storage.example.com/x.png  ")

        assertEquals("https://storage.example.com/x.png", usuario.fotoPerfilUrl)
        assertEquals("https://storage.example.com/x.png", resultado.fotoPerfilUrl)
        verify(usuarioRepository).save(usuario)
    }

    @Test
    fun `eliminarUsuario lanza cuando el usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid("uid-no")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.eliminarUsuario("uid-no")
        }
    }

    @Test
    fun `eliminarUsuario elimina solo usuario cuando no es paciente ni psicologo`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "nombre", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)

        servicio.eliminarUsuario("uid1")

        verify(usuarioRepository).delete(usuario)
        verify(pacienteRepository, never()).delete(any())
        verify(psicologoRepository, never()).delete(any())
    }
}
