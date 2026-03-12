package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.TareaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional

internal class ServicioTareaTest {

    private val tareaRepository: TareaRepository = mock()
    private val psicologoRepository: PsicologoRepository = mock()
    private val pacienteRepository: PacienteRepository = mock()

    private val servicio = ServicioTarea(tareaRepository, psicologoRepository, pacienteRepository)

    private fun crearTareaDePsicologo(uidPsicologo: String): Tarea {
        val usuarioPsi = Usuario(1L, uidPsicologo, "psi@b.com", "psi", null)
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "pac", null)
        val psicologo = Psicologo(1L, usuarioPsi, "123", "Esp")
        val paciente = Paciente(1L, usuarioPac, psicologo)
        return Tarea(1L, "Titulo", "Desc", java.time.LocalDateTime.now(), false, psicologo, paciente)
    }

    @Test
    fun `actualizarTarea lanza cuando la tarea no existe`() {
        whenever(tareaRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.actualizarTarea("uid-psi", 999L, TareaActualizarRequest("T", "D"))
        }
    }

    @Test
    fun `actualizarTarea lanza SecurityException cuando el psicologo no es el dueno`() {
        val tarea = crearTareaDePsicologo("uid-psi-otro")
        whenever(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea))

        assertThrows<SecurityException> {
            servicio.actualizarTarea("uid-psi-distinto", 1L, TareaActualizarRequest("T", "D"))
        }
    }

    @Test
    fun `actualizarTarea actualiza y devuelve cuando el psicologo es el dueno`() {
        val tarea = crearTareaDePsicologo("uid-psi")
        whenever(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea))
        whenever(tareaRepository.save(tarea)).thenReturn(tarea)

        val resultado = servicio.actualizarTarea("uid-psi", 1L, TareaActualizarRequest("Titulo nuevo", "Desc nueva"))

        assertEquals(1L, resultado.id)
        assertEquals("Titulo nuevo", resultado.titulo)
        assertEquals("Desc nueva", resultado.descripcion)
        verify(tareaRepository).save(tarea)
    }

    @Test
    fun `eliminarTarea lanza cuando la tarea no existe`() {
        whenever(tareaRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.eliminarTarea("uid-psi", 999L)
        }
    }

    @Test
    fun `eliminarTarea lanza SecurityException cuando el psicologo no es el dueno`() {
        val tarea = crearTareaDePsicologo("uid-psi-otro")
        whenever(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea))

        assertThrows<SecurityException> {
            servicio.eliminarTarea("uid-psi-distinto", 1L)
        }
    }

    @Test
    fun `eliminarTarea elimina cuando el psicologo es el dueno`() {
        val tarea = crearTareaDePsicologo("uid-psi")
        whenever(tareaRepository.findById(1L)).thenReturn(Optional.of(tarea))

        servicio.eliminarTarea("uid-psi", 1L)

        verify(tareaRepository).delete(tarea)
    }
}
