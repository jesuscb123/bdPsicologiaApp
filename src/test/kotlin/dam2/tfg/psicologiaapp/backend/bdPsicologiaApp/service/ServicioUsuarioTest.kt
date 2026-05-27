package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
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
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        val resultado = servicio.obtenerUsuarioByFireBaseId("uid1")

        assertNotNull(resultado)
        assertEquals(1L, resultado!!.id)
        assertEquals("uid1", resultado.firebaseUid)
        assertEquals("Nombre", resultado.nombre)
        assertEquals("Apellidos", resultado.apellidos)
    }

    @Test
    fun `existeCorreo devuelve true cuando el email existe`() {
        whenever(usuarioRepository.existsByEmailIgnoreCase("existente@test.com")).thenReturn(true)

        val resultado = servicio.existeCorreo("Existente@Test.com")

        assertTrue(resultado)
        verify(usuarioRepository).existsByEmailIgnoreCase("existente@test.com")
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
        val usuario = Usuario(1L, "uid1", "email@test.com", "Nombre", "Apellidos", null)
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
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        assertThrows<IllegalArgumentException> {
            servicio.actualizarEmailUsuario("uid1", "email-invalido")
        }
    }

    @Test
    fun `actualizarEmailUsuario lanza cuando el email ya esta en uso`() {
        val usuario = Usuario(1L, "uid1", "viejo@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(usuarioRepository.existsByEmailIgnoreCase("otro@b.com")).thenReturn(true)

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
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)

        assertThrows<IllegalArgumentException> {
            servicio.actualizarFotoPerfilUsuario("uid1", "no-es-una-url")
        }
    }

    @Test
    fun `actualizarFotoPerfilUsuario persiste y devuelve perfil`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
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
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)

        servicio.eliminarUsuario("uid1")

        verify(usuarioRepository).delete(usuario)
        verify(pacienteRepository, never()).delete(any())
        verify(psicologoRepository, never()).delete(any())
    }

    @Test
    fun `obtenerUsuarios devuelve lista resuelta por rol`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(usuarioRepository.findAll()).thenReturn(listOf(usuario))
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(null)

        val resultado = servicio.obtenerUsuarios()

        assertEquals(1, resultado.size)
        assertEquals("SIN_ROL", resultado[0].rol)
    }

    @Test
    fun `crearUsuario delega en servicio paciente para rol PACIENTE`() {
        val request = PacienteRequest("Nombre", "Apellidos", null, psicologoId = null)
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        val pacienteResponse = PacienteResponse(
            id = 1L, firebaseUid = "uid1", nombre = "Nombre", apellidos = "Apellidos",
            fotoPerfilUrl = null, psicologoId = null, idPaciente = 10L,
        )
        whenever(usuarioRepository.findByFirebaseUid("uid1")).thenReturn(usuario)
        whenever(servicioPaciente.crearPaciente(usuario, request)).thenReturn(pacienteResponse)

        val resultado = servicio.crearUsuario("uid1", "a@b.com", request)

        assertEquals(10L, (resultado as PacienteResponse).idPaciente)
        verify(servicioPaciente).crearPaciente(usuario, request)
    }

    @Test
    fun `crearUsuario crea entidad usuario cuando no existe y delega`() {
        val request = PacienteRequest("Nombre", "Apellidos", null, psicologoId = null)
        val usuarioNuevo = Usuario(null, "uid-nuevo", "nuevo@b.com", "Nombre", "Apellidos", null)
        val usuarioGuardado = Usuario(5L, "uid-nuevo", "nuevo@b.com", "Nombre", "Apellidos", null)
        val pacienteResponse = PacienteResponse(
            id = 5L, firebaseUid = "uid-nuevo", nombre = "Nombre", apellidos = "Apellidos",
            fotoPerfilUrl = null, psicologoId = null, idPaciente = 15L,
        )
        whenever(usuarioRepository.findByFirebaseUid("uid-nuevo")).thenReturn(null)
        whenever(usuarioRepository.save(any<Usuario>())).thenReturn(usuarioGuardado)
        whenever(servicioPaciente.crearPaciente(usuarioGuardado, request)).thenReturn(pacienteResponse)

        val resultado = servicio.crearUsuario("uid-nuevo", "nuevo@b.com", request)

        assertEquals(15L, (resultado as PacienteResponse).idPaciente)
        verify(usuarioRepository).save(any())
    }
}
