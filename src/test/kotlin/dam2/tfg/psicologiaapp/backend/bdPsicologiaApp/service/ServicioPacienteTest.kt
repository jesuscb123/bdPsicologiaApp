package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
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
        val psicologo = Psicologo(1L, usuario, "123", "Esp", null)
        val paciente = Paciente(1L, usuario, psicologo)
        whenever(pacienteRepository.findByUsuarioNombreContainingIgnoreCaseOrUsuarioApellidosContainingIgnoreCase("nombre", "nombre"))
            .thenReturn(listOf(paciente))

        val resultado = servicio.buscarPacientesPorNombre("nombre")

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].id)
        assertEquals("Nombre", resultado[0].nombre)
        assertEquals("Apellidos", resultado[0].apellidos)
    }
}
