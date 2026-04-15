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

        val nueva = Cita(
            psicologo = psicologo,
            paciente = paciente,
            inicio = inicioInstant,
            duracionMinutos = 60,
            estado = EstadoCita.RESERVADA
        )

        val guardada = try {
            citaRepository.save(nueva)
        } catch (e: DataIntegrityViolationException) {
            throw IllegalStateException("CONFLICTO_CITA_SLOT")
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
        if (horaInt !in 9..17) {
            throw IllegalStateException("La hora debe estar entre 09:00 y 17:00")
        }
    }

    private fun generarSlotsDia(): List<LocalTime> {
        return (9..17).map { LocalTime.of(it, 0) }
    }
}

