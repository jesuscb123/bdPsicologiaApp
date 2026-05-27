package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional

internal class ServicioPsicologoTest {

    private val psicologoRepository: PsicologoRepository = mock()
    private val pacienteRepository: PacienteRepository = mock()

    private val servicio = ServicioPsicologo(psicologoRepository, pacienteRepository)

    @Test
    fun `buscarPsicologosPorNombre devuelve lista vacia cuando nombre en blanco`() {
        val resultado = servicio.buscarPsicologosPorNombre("")

        assertTrue(resultado.isEmpty())
        verify(psicologoRepository, never()).findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase(any(), any())
    }

    @Test
    fun `buscarPsicologosPorNombre devuelve lista cuando hay resultados`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Doctor", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        whenever(psicologoRepository.findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase("doctor", "doctor"))
            .thenReturn(listOf(psicologo))

        val resultado = servicio.buscarPsicologosPorNombre("doctor")

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].id)
        assertEquals("Doctor", resultado[0].nombre)
        assertEquals("Apellidos", resultado[0].apellidos)
    }

    @Test
    fun `obtenerEntidadPsicologo lanza cuando no existe`() {
        whenever(psicologoRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.obtenerEntidadPsicologo(999L)
        }
    }

    @Test
    fun `obtenerPsicologos devuelve lista mapeada`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Doctor", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        whenever(psicologoRepository.findAll()).thenReturn(listOf(psicologo))

        val resultado = servicio.obtenerPsicologos()

        assertEquals(1, resultado.size)
        assertEquals("Doctor", resultado[0].nombre)
    }

    @Test
    fun `crearPsicologo guarda nuevo psicologo`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Doctor", "Apellidos", null)
        whenever(pacienteRepository.existsByUsuario(usuario)).thenReturn(false)
        whenever(psicologoRepository.existsByUsuario(usuario)).thenReturn(false)
        whenever(psicologoRepository.save(any<Psicologo>())).thenAnswer {
            val p = it.getArgument<Psicologo>(0)
            Psicologo(5L, p.usuario, p.numeroColegiado, p.especialidades, p.descripcion)
        }

        val resultado = servicio.crearPsicologo(
            usuario,
            PsicologoRequest(
                nombre = "Doctor",
                apellidos = "Apellidos",
                fotoPerfilUrl = null,
                numeroColegiado = "COL-99",
                especialidades = listOf("Clínica"),
                descripcion = "Desc",
            ),
        )

        assertEquals(5L, resultado.idEntidadPsicologo)
        assertEquals("COL-99", resultado.numeroColegiado)
        verify(psicologoRepository).save(any())
    }

    @Test
    fun `crearPsicologo lanza cuando el usuario ya es paciente`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "Nombre", "Apellidos", null)
        whenever(pacienteRepository.existsByUsuario(usuario)).thenReturn(true)

        assertThrows<IllegalStateException> {
            servicio.crearPsicologo(
                usuario,
                PsicologoRequest(
                    nombre = "N",
                    apellidos = "A",
                    fotoPerfilUrl = null,
                    numeroColegiado = "123",
                    especialidades = listOf("Esp"),
                    descripcion = null,
                ),
            )
        }
    }

    @Test
    fun `obtenerPacientesPorFirebaseId devuelve vacio cuando el psicologo no existe`() {
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-no")).thenReturn(null)

        assertTrue(servicio.obtenerPacientesPorFirebaseId("uid-no").isEmpty())
    }

    @Test
    fun `obtenerPacientesPorFirebaseId devuelve pacientes asignados`() {
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Logo", null)
        val psicologo = Psicologo(10L, usuarioPsi, "123", mutableListOf("Esp"), null)
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Ente", null)
        val paciente = Paciente(20L, usuarioPac, psicologo)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(psicologo)
        whenever(pacienteRepository.findAllByPsicologo(psicologo)).thenReturn(listOf(paciente))

        val resultado = servicio.obtenerPacientesPorFirebaseId("uid-psi")

        assertEquals(1, resultado.size)
        assertEquals(20L, resultado[0].idPaciente)
    }
}
