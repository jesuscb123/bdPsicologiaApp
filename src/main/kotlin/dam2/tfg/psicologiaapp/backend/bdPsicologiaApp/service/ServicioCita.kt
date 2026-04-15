package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Cita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.CitaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.DisponibilidadResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.CitaMapper
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*

@Service
class ServicioCita(
    private val citaRepository: CitaRepository,
    private val pacienteRepository: PacienteRepository,
    private val reloj: Clock = Clock.systemUTC(),
) : IServicioCita {

    @Transactional(readOnly = true)
    override fun obtenerDisponibilidadDia(firebaseUidPaciente: String, fecha: LocalDate, zonaHoraria: String): DisponibilidadResponse {
        val paciente = obtenerPacientePorFirebaseUid(firebaseUidPaciente)
        val psicologo = paciente.psicologo ?: throw IllegalStateException("El paciente no tiene psicólogo asignado")

        val zoneId = ZoneId.of(zonaHoraria)
        validarDiaLaborable(fecha)

        val inicioDiaUtc = fecha.atStartOfDay(zoneId).toInstant()
        val inicioDiaSiguienteUtc = fecha.plusDays(1).atStartOfDay(zoneId).toInstant()

        val citasReservadas = citaRepository.findByPsicologoIdAndInicioEnRangoAndEstado(
            psicologoId = psicologo.id ?: throw IllegalStateException("El psicólogo no tiene ID"),
            inicioDesde = inicioDiaUtc,
            inicioHasta = inicioDiaSiguienteUtc,
            estado = EstadoCita.RESERVADA
        )

        val horasOcupadas = citasReservadas
            .map { it.inicio.atZone(zoneId).toLocalTime().withMinute(0).withSecond(0).withNano(0) }
            .toSet()

        val horasDisponibles = generarSlotsDia()
            .filterNot { it in horasOcupadas }

        return DisponibilidadResponse(
            fecha = fecha,
            zonaHoraria = zonaHoraria,
            horasDisponibles = horasDisponibles
        )
    }

    @Transactional
    override fun reservarCita(firebaseUidPaciente: String, request: CitaCrearRequest): CitaResponse {
        val paciente = obtenerPacientePorFirebaseUid(firebaseUidPaciente)
        val psicologo = paciente.psicologo ?: throw IllegalStateException("El paciente no tiene psicólogo asignado")

        val zoneId = ZoneId.of(request.zonaHoraria)

        val inicioInstant = request.inicio.toInstant()
        val inicioLocal = inicioInstant.atZone(zoneId)

        validarDiaLaborable(inicioLocal.toLocalDate())
        validarHoraSlot(inicioLocal.toLocalTime())

        val psicologoId = psicologo.id ?: throw IllegalStateException("El psicólogo no tiene ID")

        // Mismo instante en BD puede no coincidir bit a bit con el del request (precisión/persistencia).
        // Buscamos por ventana de slot [inicioLocal normalizado, +1h) en la zona del paciente.
        val fechaSlot = inicioLocal.toLocalDate()
        val horaSlot = inicioLocal.toLocalTime().withSecond(0).withNano(0)
        val inicioSlot = ZonedDateTime.of(fechaSlot, horaSlot, zoneId).toInstant()
        val finSlot = inicioSlot.plusSeconds(3600L)

        // Restricción unique (psicologo_id, inicio). Si hay fila CANCELADA en ese slot, se reutiliza.
        val existente = citaRepository
            .findByPsicologoIdAndInicioEnRango(psicologoId = psicologoId, inicioDesde = inicioSlot, inicioHasta = finSlot)
            .firstOrNull()

        val guardada = when (existente?.estado) {
            EstadoCita.RESERVADA -> throw IllegalStateException("CONFLICTO_CITA_SLOT")
            EstadoCita.CANCELADA -> {
                existente.paciente = paciente
                existente.duracionMinutos = 60
                existente.inicio = inicioSlot
                existente.estado = EstadoCita.RESERVADA
                citaRepository.save(existente)
            }
            null -> {
                val nueva = Cita(
                    psicologo = psicologo,
                    paciente = paciente,
                    inicio = inicioSlot,
                    duracionMinutos = 60,
                    estado = EstadoCita.RESERVADA
                )
                try {
                    citaRepository.save(nueva)
                } catch (e: DataIntegrityViolationException) {
                    val reintento = citaRepository
                        .findByPsicologoIdAndInicioEnRango(
                            psicologoId = psicologoId,
                            inicioDesde = inicioSlot,
                            inicioHasta = finSlot,
                        )
                        .firstOrNull()
                    if (reintento?.estado == EstadoCita.CANCELADA) {
                        reintento.paciente = paciente
                        reintento.duracionMinutos = 60
                        reintento.inicio = inicioSlot
                        reintento.estado = EstadoCita.RESERVADA
                        citaRepository.save(reintento)
                    } else {
                        throw IllegalStateException("CONFLICTO_CITA_SLOT")
                    }
                }
            }
        }

        return CitaMapper.toResponse(guardada, reloj)
    }

    @Transactional
    override fun cancelarCita(firebaseUidPaciente: String, citaId: Long): CitaResponse {
        val cita = citaRepository.findByIdOrNull(citaId) ?: throw IllegalStateException("La cita no existe")

        if (cita.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para cancelar esta cita")
        }

        if (cita.estado == EstadoCita.CANCELADA) {
            return CitaMapper.toResponse(cita, reloj)
        }

        val fin = cita.inicio.plusSeconds(cita.duracionMinutos.toLong() * 60L)
        if (reloj.instant().isAfter(fin)) {
            throw IllegalStateException("No se puede cancelar una cita finalizada")
        }

        cita.estado = EstadoCita.CANCELADA
        val guardada = citaRepository.save(cita)
        return CitaMapper.toResponse(guardada, reloj)
    }

    @Transactional(readOnly = true)
    override fun obtenerMisCitasPaciente(firebaseUidPaciente: String): List<CitaResponse> {
        val citas = citaRepository.findCitasByPacienteFirebaseUid(firebaseUidPaciente)
        return citas.map { CitaMapper.toResponse(it, reloj) }
    }

    @Transactional(readOnly = true)
    override fun obtenerMisCitasPsicologo(firebaseUidPsicologo: String): List<CitaResponse> {
        // Si no es psicólogo, esto fallará al no existir en BD (misma idea que en tareas).
        // No hace falta resolver entidad aquí porque la query ya filtra por psicólogo.firebaseUid
        val citas = citaRepository.findCitasByPsicologoFirebaseUid(firebaseUidPsicologo)
        return citas.map { CitaMapper.toResponse(it, reloj) }
    }

    private fun obtenerPacientePorFirebaseUid(firebaseUidPaciente: String): Paciente {
        return pacienteRepository.findByIdFirebaseUsuario(firebaseUidPaciente)
            ?: throw SecurityException("No autorizado: el usuario no es paciente")
    }

    private fun validarDiaLaborable(fecha: LocalDate) {
        val dia = fecha.dayOfWeek
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            throw IllegalStateException("Solo se permiten citas en días laborables (L-V)")
        }
    }

    private fun validarHoraSlot(hora: LocalTime) {
        val horaNormalizada = hora.withSecond(0).withNano(0)
        if (horaNormalizada.minute != 0) {
            throw IllegalStateException("La hora de inicio debe ser exacta (minutos=0)")
        }

        val horaInt = horaNormalizada.hour
        // Intervalo de atención 09:00–17:00 (último inicio posible 16:00 para una duración de 60 min)
        if (horaInt !in 9..16) {
            throw IllegalStateException("La hora debe estar entre 09:00 y 17:00")
        }
    }

    private fun generarSlotsDia(): List<LocalTime> {
        // Slots de inicio por hora (duración 60 min): 09:00, ..., 16:00
        return (9..16).map { LocalTime.of(it, 0) }
    }
}

