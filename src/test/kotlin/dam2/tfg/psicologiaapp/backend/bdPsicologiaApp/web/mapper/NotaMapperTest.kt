package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto.NotaRequest
import java.time.LocalDateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class NotaMapperTest {

    private val usuarioPac = Usuario(1L, "uid-pac", "pac@test.com", "Pac", "Apellidos", null)
    private val usuarioPsi = Usuario(2L, "uid-psi", "psi@test.com", "Psi", "Apellidos", null)
    private val psicologo = Psicologo(10L, usuarioPsi, "12345", mutableListOf("Clinica"), null)
    private val paciente = Paciente(20L, usuarioPac, psicologo)

    @Test
    fun `toEntity crea nota con paciente y psicologo`() {
        val request = NotaRequest("Asunto", "Descripción")

        val entity = NotaMapper.toEntity(request, paciente, psicologo)

        assertEquals("Asunto", entity.asunto)
        assertEquals("Descripción", entity.descripcion)
        assertSame(paciente, entity.paciente)
        assertSame(psicologo, entity.psicologo)
    }

    @Test
    fun `toResponse mapea nota completa`() {
        val fecha = LocalDateTime.of(2026, 5, 27, 10, 0)
        val nota = Nota(5L, "Asunto", "Desc", paciente, psicologo, fecha)

        val response = NotaMapper.toResponse(nota)

        assertEquals(5L, response.id)
        assertEquals("Asunto", response.asunto)
        assertEquals("Desc", response.descripcion)
        assertEquals(fecha, response.ultimaModificacion)
        assertEquals(20L, response.paciente.idPaciente)
        assertEquals(10L, response.psicologo.idEntidadPsicologo)
    }

    @Test
    fun `toRequest extrae asunto y descripcion`() {
        val nota = Nota(1L, "Titulo", "Texto", paciente, psicologo)

        val request = NotaMapper.toRequest(nota)

        assertEquals("Titulo", request.asunto)
        assertEquals("Texto", request.descripcion)
    }
}
