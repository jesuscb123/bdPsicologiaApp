package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Cita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.EstadoCitaCalculado
import java.time.Clock
import java.time.OffsetDateTime
import java.time.ZoneOffset

object CitaMapper {
    fun toResponse(cita: Cita, reloj: Clock = Clock.systemUTC()): CitaResponse {
        val id = cita.id ?: throw IllegalStateException("La cita no tiene ID")

        val inicioUtc = OffsetDateTime.ofInstant(cita.inicio, ZoneOffset.UTC)
        val finUtc = OffsetDateTime.ofInstant(cita.inicio.plusSeconds(cita.duracionMinutos.toLong() * 60L), ZoneOffset.UTC)

        val ahora = reloj.instant()
        val estadoCalculado = when (cita.estado) {
            EstadoCita.CANCELADA -> EstadoCitaCalculado.CANCELADA
            EstadoCita.RESERVADA -> {
                val finInstant = cita.inicio.plusSeconds(cita.duracionMinutos.toLong() * 60L)
                if (ahora.isAfter(finInstant)) EstadoCitaCalculado.FINALIZADA else EstadoCitaCalculado.ACTIVA
            }
        }

        return CitaResponse(
            id = id,
            inicio = inicioUtc,
            fin = finUtc,
            psicologo = PsicologoMapper.toResponse(cita.psicologo),
            paciente = PacienteMapper.toResponse(cita.paciente),
            estadoPersistido = cita.estado,
            estadoCalculado = estadoCalculado
        )
    }
}

