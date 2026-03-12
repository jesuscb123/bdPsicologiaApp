package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional

internal class ServicioNotaTest {

    private val notaRepository: NotaRepository = mock()
    private val servicioPaciente: IServicioPaciente = mock()
    private val servicioPsicologo: IServicioPsicologo = mock()

    private val servicio = ServicioNota(notaRepository, servicioPaciente, servicioPsicologo)

    @Test
    fun `actualizarNota lanza cuando la nota no existe`() {
        whenever(notaRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.actualizarNota("uid-paciente", 999L, NotaRequest("Asunto", "Desc"))
        }
    }

    @Test
    fun `actualizarNota lanza SecurityException cuando el paciente no es el dueno`() {
        val usuario = Usuario(1L, "uid-otro", "a@b.com", "otro", null)
        val psicologo = Psicologo(1L, usuario, "123", "Especialidad")
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "Asunto", "Desc", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        assertThrows<SecurityException> {
            servicio.actualizarNota("uid-paciente-distinto", 1L, NotaRequest("Nuevo", "Nueva desc"))
        }
    }

    @Test
    fun `actualizarNota actualiza y devuelve cuando el paciente es el dueno`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "paciente", null)
        val psicologo = Psicologo(1L, usuario, "123", "Esp")
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "Asunto viejo", "Desc vieja", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))
        whenever(notaRepository.save(nota)).thenReturn(nota)

        val resultado = servicio.actualizarNota("uid-paciente", 1L, NotaRequest("Asunto nuevo", "Desc nueva"))

        assertEquals(1L, resultado.id)
        assertEquals("Asunto nuevo", resultado.asunto)
        assertEquals("Desc nueva", resultado.descripcion)
        verify(notaRepository).save(nota)
    }

    @Test
    fun `eliminarNota lanza cuando la nota no existe`() {
        whenever(notaRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.eliminarNota("uid-paciente", 999L)
        }
    }

    @Test
    fun `eliminarNota lanza SecurityException cuando el paciente no es el dueno`() {
        val usuario = Usuario(1L, "uid-otro", "a@b.com", "otro", null)
        val psicologo = Psicologo(1L, usuario, "123", "Esp")
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "A", "D", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        assertThrows<SecurityException> {
            servicio.eliminarNota("uid-distinto", 1L)
        }
    }

    @Test
    fun `eliminarNota elimina cuando el paciente es el dueno`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "paciente", null)
        val psicologo = Psicologo(1L, usuario, "123", "Esp")
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "A", "D", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        servicio.eliminarNota("uid-paciente", 1L)

        verify(notaRepository).delete(nota)
    }
}
