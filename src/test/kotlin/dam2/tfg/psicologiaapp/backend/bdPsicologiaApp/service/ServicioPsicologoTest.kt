package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
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
        verify(psicologoRepository, never()).findByUsuarioNombreUsuarioContainingIgnoreCase(any())
    }

    @Test
    fun `buscarPsicologosPorNombre devuelve lista cuando hay resultados`() {
        val usuario = Usuario(1L, "uid1", "a@b.com", "doctor", null)
        val psicologo = Psicologo(1L, usuario, "123", "Esp")
        whenever(psicologoRepository.findByUsuarioNombreUsuarioContainingIgnoreCase("doctor"))
            .thenReturn(listOf(psicologo))

        val resultado = servicio.buscarPsicologosPorNombre("doctor")

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].id)
        assertEquals("doctor", resultado[0].nombreUsuario)
    }

    @Test
    fun `obtenerEntidadPsicologo lanza cuando no existe`() {
        whenever(psicologoRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.obtenerEntidadPsicologo(999L)
        }
    }
}
