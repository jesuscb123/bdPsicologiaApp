package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TareaMapperTest {

    private val usuarioPac = Usuario(1L, "uid-pac", "pac@test.com", "Pac", "Apellidos", null)
    private val usuarioPsi = Usuario(2L, "uid-psi", "psi@test.com", "Psi", "Apellidos", null)
    private val psicologo = Psicologo(10L, usuarioPsi, "12345", mutableListOf("Clinica"), null)
    private val paciente = Paciente(20L, usuarioPac, psicologo)

    @Test
    fun `toEntity crea tarea con titulo y descripcion`() {
        val request = TareaCrearRequest("Hacer ejercicio", "30 minutos al día")

        val entity = TareaMapper.toEntity(request, psicologo, paciente)

        assertEquals("Hacer ejercicio", entity.tituloTarea)
        assertEquals("30 minutos al día", entity.descripcionTarea)
        assertSame(psicologo, entity.psicologo)
        assertSame(paciente, entity.paciente)
        assertFalse(entity.realizada)
        assertFalse(entity.aceptadaPorPaciente)
    }

    @Test
    fun `toResponse mapea tarea completa`() {
        val hora = LocalDateTime.of(2026, 5, 27, 9, 0)
        val tarea = Tarea(
            id = 7L,
            tituloTarea = "Tarea",
            descripcionTarea = "Desc",
            horaEnvio = hora,
            realizada = true,
            aceptadaPorPaciente = true,
            psicologo = psicologo,
            paciente = paciente,
        )

        val response = TareaMapper.toResponse(tarea)

        assertEquals(7L, response.id)
        assertEquals("Tarea", response.titulo)
        assertEquals("Desc", response.descripcion)
        assertEquals(hora, response.horaEnvio)
        assertTrue(response.realizada)
        assertTrue(response.aceptadaPorPaciente)
        assertEquals(20L, response.paciente.idPaciente)
    }

    @Test
    fun `toResponse lanza cuando falta id de tarea`() {
        val tarea = Tarea(
            id = null,
            tituloTarea = "T",
            descripcionTarea = "D",
            psicologo = psicologo,
            paciente = paciente,
        )

        assertThrows<IllegalStateException> {
            TareaMapper.toResponse(tarea)
        }
    }
}
