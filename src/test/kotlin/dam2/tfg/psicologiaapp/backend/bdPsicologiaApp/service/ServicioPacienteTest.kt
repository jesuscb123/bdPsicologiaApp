package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

internal class ServicioPacienteTest {

    private val pacienteRepository: PacienteRepository = mock()
    private val servicioPsicologo: IServicioPsicologo = mock()

    private val servicio = ServicioPaciente(pacienteRepository, servicioPsicologo)

    @Test
    fun `buscarPacientesPorNombre devuelve lista vacia cuando nombre en blanco`() {
        val resultado = servicio.buscarPacientesPorNombre("   ")

        assertTrue(resultado.isEmpty())
        verify(pacienteRepository, never()).findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase(any(), any())
    }

    @Test
    fun `buscarPacientesPorNombre devuelve lista de pacientes cuando hay resultados`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(1L, usuario, psicologo)
        whenever(pacienteRepository.findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase("nombre", "nombre"))
            .thenReturn(listOf(paciente))

        val resultado = servicio.buscarPacientesPorNombre("nombre")

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].id)
        assertEquals("Nombre", resultado[0].nombre)
        assertEquals("Apellidos", resultado[0].apellidos)
    }

    @Test
    fun `obtenerPacienteFirebaseId devuelve null cuando no existe`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-no")).thenReturn(null)

        assertNull(servicio.obtenerPacienteFirebaseId("uid-no"))
    }

    @Test
    fun `obtenerPacienteFirebaseId devuelve response cuando existe`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        val paciente = Paciente(5L, usuario, null)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid1")).thenReturn(paciente)

        val resultado = servicio.obtenerPacienteFirebaseId("uid1")

        assertNotNull(resultado)
        assertEquals(5L, resultado!!.idPaciente)
    }

    @Test
    fun `crearPaciente guarda paciente sin psicologo`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(pacienteRepository.existsByUsuario(usuario)).thenReturn(false)
        whenever(pacienteRepository.save(any<Paciente>())).thenAnswer {
            val p = it.getArgument<Paciente>(0)
            Paciente(10L, p.usuario, p.psicologo)
        }

        val resultado = servicio.crearPaciente(
            usuario,
            PacienteRequest("Nombre", "Apellidos", null, psicologoId = null),
        )

        assertEquals(10L, resultado.idPaciente)
        assertNull(resultado.psicologoId)
        verify(pacienteRepository).save(any())
    }

    @Test
    fun `crearPaciente lanza cuando el usuario ya es paciente`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(pacienteRepository.existsByUsuario(usuario)).thenReturn(true)

        assertThrows<IllegalStateException> {
            servicio.crearPaciente(usuario, PacienteRequest("N", "A", null, psicologoId = null))
        }
    }

    @Test
    fun `obtenerPacientesAsignadosA delega en servicio psicologo`() {
        val lista = listOf(
            PacienteResponse(
                id = 2L, firebaseUid = "uid-p", nombre = "P", apellidos = "A",
                fotoPerfilUrl = null, psicologoId = 1L, idPaciente = 20L,
            ),
        )
        whenever(servicioPsicologo.obtenerPacientesPorFirebaseId("uid-psi")).thenReturn(lista)

        val resultado = servicio.obtenerPacientesAsignadosA("uid-psi")

        assertEquals(1, resultado.size)
        verify(servicioPsicologo).obtenerPacientesPorFirebaseId("uid-psi")
    }

    @Test
    fun `actualizarPsicologo asigna psicologo al paciente`() {
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Ente", null)
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Logo", null)
        val psicologo = Psicologo(10L, usuarioPsi, "COL", mutableListOf("Esp"), null)
        val paciente = Paciente(20L, usuarioPac, null)
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(servicioPsicologo.obtenerEntidadPsicologo(10L)).thenReturn(psicologo)
        whenever(pacienteRepository.save(paciente)).thenReturn(paciente)

        val resultado = servicio.actualizarPsicologo("uid-pac", 10L)

        assertEquals(10L, resultado.psicologoId)
        assertEquals(psicologo, paciente.psicologo)
    }
}
