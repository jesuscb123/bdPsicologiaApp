package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PacienteMapperTest {

    private val usuarioPac = Usuario(1L, "uid-pac", "pac@test.com", "Paciente", "Apellidos", null)
    private val usuarioPsi = Usuario(2L, "uid-psi", "psi@test.com", "Psi", "Apellidos", null)
    private val psicologo = Psicologo(10L, usuarioPsi, "12345", mutableListOf("Clinica"), null)

    @Test
    fun `toEntity vincula usuario y psicologo`() {
        val request = PacienteRequest("Paciente", "Apellidos", null, "PACIENTE", 10L)

        val entity = PacienteMapper.toEntity(request, psicologo, usuarioPac)

        assertSame(usuarioPac, entity.usuario)
        assertSame(psicologo, entity.psicologo)
        assertNull(entity.id)
    }

    @Test
    fun `toResponse mapea campos del paciente`() {
        val paciente = Paciente(20L, usuarioPac, psicologo)

        val response = PacienteMapper.toResponse(paciente)

        assertEquals(1L, response.id)
        assertEquals("uid-pac", response.firebaseUid)
        assertEquals("Paciente", response.nombre)
        assertEquals(10L, response.psicologoId)
        assertEquals(20L, response.idPaciente)
    }

    @Test
    fun `toResponse lanza cuando falta id de paciente`() {
        val paciente = Paciente(null, usuarioPac, psicologo)

        assertThrows<IllegalStateException> {
            PacienteMapper.toResponse(paciente)
        }
    }
}
