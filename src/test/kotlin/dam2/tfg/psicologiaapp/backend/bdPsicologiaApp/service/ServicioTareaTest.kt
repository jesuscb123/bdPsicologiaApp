package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.TareaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.Optional

internal class ServicioTareaTest {

    private val tareaRepository: TareaRepository = mock()
    private val psicologoRepository: PsicologoRepository = mock()
    private val pacienteRepository: PacienteRepository = mock()
    private val servicioNotificacionesPush: IServicioNotificacionesPush = mock()

    private val servicio = ServicioTarea(
        tareaRepository,
        psicologoRepository,
        pacienteRepository,
        servicioNotificacionesPush,
    )

    private fun crearTareaDePsicologo(uidPsicologo: String): Tarea {
        val usuarioPsi = Usuario(1L, uidPsicologo, "psi@b.com", "Psi", "Apellidos", null)
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Apellidos", null)
        val psicologo = Psicologo(1L, usuarioPsi, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(1L, usuarioPac, psicologo)
        return Tarea(1L, "Titulo", "Desc", java.time.LocalDateTime.now(), false, false, psicologo, paciente)
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

    @Test
    fun `crearTarea guarda tarea cuando el paciente pertenece al psicologo`() {
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Apellidos", null)
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Apellidos", null)
        val psicologo = Psicologo(1L, usuarioPsi, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(2L, usuarioPac, psicologo)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(psicologo)
        whenever(pacienteRepository.findById(2L)).thenReturn(Optional.of(paciente))
        whenever(tareaRepository.save(any<Tarea>())).thenAnswer {
            val t = it.getArgument<Tarea>(0)
            t.id = 50L
            t
        }

        val resultado = servicio.crearTarea(
            "uid-psi",
            2L,
            TareaCrearRequest("Nueva tarea", "Descripción"),
        )

        assertEquals(50L, resultado.id)
        assertEquals("Nueva tarea", resultado.titulo)
        verify(tareaRepository).save(any())
    }

    @Test
    fun `crearTarea lanza SecurityException cuando el paciente no es del psicologo`() {
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Apellidos", null)
        val otroPsi = Psicologo(99L, Usuario(3L, "uid-otro", "o@b.com", "O", "P", null), "999", mutableListOf("Otra"), null)
        val paciente = Paciente(2L, Usuario(2L, "uid-pac", "pac@b.com", "Pac", "A", null), otroPsi)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(
            Psicologo(1L, usuarioPsi, "123", mutableListOf("Esp"), null),
        )
        whenever(pacienteRepository.findById(2L)).thenReturn(Optional.of(paciente))

        assertThrows<SecurityException> {
            servicio.crearTarea("uid-psi", 2L, TareaCrearRequest("T", "D"))
        }
    }

    @Test
    fun `obtenerTareasPaciente devuelve lista de tareas`() {
        val tarea = crearTareaDePsicologo("uid-psi")
        whenever(tareaRepository.findTareasByPacienteFirebaseUid("uid-pac")).thenReturn(listOf(tarea))

        val resultado = servicio.obtenerTareasPaciente("uid-pac")

        assertEquals(1, resultado.size)
    }

    @Test
    fun `obtenerEstadoTareasPaciente devuelve estado sync`() {
        val estado = mock<TareaRepository.EstadoTareasProjection>()
        whenever(estado.ultimaModificacion).thenReturn(LocalDateTime.of(2025, 6, 1, 9, 0))
        whenever(estado.total).thenReturn(2L)
        whenever(tareaRepository.obtenerEstadoTareasPaciente("uid-pac")).thenReturn(estado)

        val resultado = servicio.obtenerEstadoTareasPaciente("uid-pac")

        assertEquals(2L, resultado.total)
    }
}
