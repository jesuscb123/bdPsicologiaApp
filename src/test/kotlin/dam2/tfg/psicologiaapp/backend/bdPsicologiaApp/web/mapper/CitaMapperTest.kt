package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Cita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.EstadoCitaCalculado
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CitaMapperTest {

    private val usuarioPac = Usuario(1L, "uid-pac", "pac@test.com", "Pac", "Apellidos", null)
    private val usuarioPsi = Usuario(2L, "uid-psi", "psi@test.com", "Psi", "Apellidos", null)
    private val psicologo = Psicologo(10L, usuarioPsi, "12345", mutableListOf("Clinica"), null)
    private val paciente = Paciente(20L, usuarioPac, psicologo)

    @Test
    fun `toResponse mapea cita reservada activa`() {
        val inicio = Instant.parse("2026-05-27T10:00:00Z")
        val reloj = Clock.fixed(Instant.parse("2026-05-27T10:30:00Z"), ZoneOffset.UTC)
        val cita = Cita(1L, psicologo, paciente, inicio, 60, EstadoCita.RESERVADA)

        val response = CitaMapper.toResponse(cita, reloj)

        assertEquals(1L, response.id)
        assertEquals(EstadoCita.RESERVADA, response.estadoPersistido)
        assertEquals(EstadoCitaCalculado.ACTIVA, response.estadoCalculado)
        assertEquals(20L, response.paciente.idPaciente)
        assertEquals(10L, response.psicologo.idEntidadPsicologo)
    }

    @Test
    fun `toResponse calcula FINALIZADA cuando la cita ya paso`() {
        val inicio = Instant.parse("2026-05-27T08:00:00Z")
        val reloj = Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneOffset.UTC)
        val cita = Cita(2L, psicologo, paciente, inicio, 60, EstadoCita.RESERVADA)

        val response = CitaMapper.toResponse(cita, reloj)

        assertEquals(EstadoCitaCalculado.FINALIZADA, response.estadoCalculado)
    }

    @Test
    fun `toResponse calcula CANCELADA cuando estado persistido es cancelada`() {
        val cita = Cita(3L, psicologo, paciente, Instant.parse("2026-05-27T10:00:00Z"), 60, EstadoCita.CANCELADA)

        val response = CitaMapper.toResponse(cita)

        assertEquals(EstadoCitaCalculado.CANCELADA, response.estadoCalculado)
    }

    @Test
    fun `toResponse lanza cuando falta id de cita`() {
        val cita = Cita(null, psicologo, paciente, Instant.now(), 60, EstadoCita.RESERVADA)

        assertThrows<IllegalStateException> {
            CitaMapper.toResponse(cita)
        }
    }
}
