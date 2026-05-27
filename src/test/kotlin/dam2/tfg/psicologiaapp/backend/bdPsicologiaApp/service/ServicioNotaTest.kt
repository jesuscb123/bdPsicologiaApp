package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo.IServicioDeteccionRiesgo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto.NotaRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.Optional

internal class ServicioNotaTest {

    private val notaRepository: NotaRepository = mock()
    private val servicioPaciente: IServicioPaciente = mock()
    private val servicioPsicologo: IServicioPsicologo = mock()
    private val servicioDeteccionRiesgo: IServicioDeteccionRiesgo = mock()

    private val servicio = ServicioNota(
        notaRepository,
        servicioPaciente,
        servicioPsicologo,
        servicioDeteccionRiesgo,
    )

    @Test
    fun `actualizarNota lanza cuando la nota no existe`() {
        whenever(notaRepository.findById(999L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.actualizarNota("uid-paciente", 999L, NotaRequest("Asunto", "Desc"))
        }
    }

    @Test
    fun `actualizarNota lanza SecurityException cuando el paciente no es el dueno`() {
        val usuario = Usuario(1L, "uid-otro", "a@b.com", "Otro", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Especialidad"), null)
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "Asunto", "Desc", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        assertThrows<SecurityException> {
            servicio.actualizarNota("uid-paciente-distinto", 1L, NotaRequest("Nuevo", "Nueva desc"))
        }
    }

    @Test
    fun `actualizarNota actualiza y devuelve cuando el paciente es el dueno`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "Paciente", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
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
        val usuario = Usuario(1L, "uid-otro", "a@b.com", "Otro", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "A", "D", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        assertThrows<SecurityException> {
            servicio.eliminarNota("uid-distinto", 1L)
        }
    }

    @Test
    fun `eliminarNota elimina cuando el paciente es el dueno`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "Paciente", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(1L, "A", "D", paciente, psicologo)
        whenever(notaRepository.findById(1L)).thenReturn(Optional.of(nota))

        servicio.eliminarNota("uid-paciente", 1L)

        verify(notaRepository).delete(nota)
    }

    @Test
    fun `crearNota guarda nota y dispara deteccion de riesgo`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "Paciente", "Apellidos", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("Esp"), null)
        val paciente = Paciente(10L, usuario, psicologo)
        val notaGuardada = Nota(1L, "Asunto", "Desc", paciente, psicologo)
        whenever(servicioPaciente.obtenerEntidadPacientePorFirebaseId("uid-paciente")).thenReturn(paciente)
        whenever(notaRepository.save(any<Nota>())).thenReturn(notaGuardada)

        val resultado = servicio.crearNota("uid-paciente", NotaRequest("Asunto", "Desc"))

        assertEquals(1L, resultado.id)
        verify(notaRepository).save(any())
        verify(servicioDeteccionRiesgo).evaluarRiesgoUltimasNotasAsync(10L)
    }

    @Test
    fun `crearNota lanza cuando el paciente no tiene psicologo`() {
        val usuario = Usuario(1L, "uid-paciente", "a@b.com", "Paciente", "Apellidos", null)
        val paciente = Paciente(10L, usuario, null)
        whenever(servicioPaciente.obtenerEntidadPacientePorFirebaseId("uid-paciente")).thenReturn(paciente)

        assertThrows<IllegalStateException> {
            servicio.crearNota("uid-paciente", NotaRequest("A", "D"))
        }
    }

    @Test
    fun `obtenerNotasPaciente devuelve notas del paciente`() {
        val usuario = Usuario(1L, "uid-pac", "a@b.com", "P", "A", null)
        val psicologo = Psicologo(1L, usuario, "123", mutableListOf("E"), null)
        val paciente = Paciente(1L, usuario, psicologo)
        val nota = Nota(5L, "Asunto", "Desc", paciente, psicologo)
        whenever(notaRepository.obtenerNotasByPacienteUsuarioFirebaseId("uid-pac")).thenReturn(listOf(nota))

        val resultado = servicio.obtenerNotasPaciente("uid-pac")

        assertEquals(1, resultado.size)
        assertEquals(5L, resultado[0].id)
    }

    @Test
    fun `obtenerEstadoNotasPaciente devuelve estado sync`() {
        val estado = mock<NotaRepository.EstadoNotasProjection>()
        whenever(estado.ultimaModificacion).thenReturn(LocalDateTime.of(2025, 5, 1, 12, 0))
        whenever(estado.total).thenReturn(4L)
        whenever(notaRepository.obtenerEstadoNotasPaciente("uid-pac")).thenReturn(estado)

        val resultado = servicio.obtenerEstadoNotasPaciente("uid-pac")

        assertEquals(4L, resultado.total)
        assertEquals(LocalDateTime.of(2025, 5, 1, 12, 0), resultado.ultimaModificacion)
    }

    @Test
    fun `obtenerNotasPacienteParaPsicologo lanza SecurityException sin permiso`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId("uid-psi")).thenReturn(
            PsicologoResponse(
                id = 1L, idEntidadPsicologo = 10L, firebaseUid = "uid-psi",
                nombre = "P", apellidos = "A", fotoPerfilUrl = null,
                numeroColegiado = "123", especialidades = listOf("E"), descripcion = null,
            ),
        )
        whenever(servicioPaciente.obtenerPacienteId(20L)).thenReturn(
            PacienteResponse(
                id = 2L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Ente",
                fotoPerfilUrl = null, psicologoId = 99L, idPaciente = 20L,
            ),
        )

        assertThrows<SecurityException> {
            servicio.obtenerNotasPacienteParaPsicologo("uid-psi", 20L)
        }
    }
}
