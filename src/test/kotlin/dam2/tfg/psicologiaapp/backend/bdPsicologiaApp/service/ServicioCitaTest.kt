package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Cita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.CitaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.EstadoCitaCalculado
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.*
import java.util.Optional

internal class ServicioCitaTest {

    private val citaRepository: CitaRepository = mock()
    private val pacienteRepository: PacienteRepository = mock()

    private val zona = "Europe/Madrid"
    private val martesLaborable = LocalDate.of(2025, 5, 27)
    private val sabado = LocalDate.of(2025, 5, 31)

    private fun relojFijo(instante: Instant = Instant.parse("2025-05-27T08:00:00Z")): Clock =
        Clock.fixed(instante, ZoneId.of("UTC"))

    private fun pacienteConPsicologo(): Paciente {
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Ente", null)
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Logo", null)
        val psicologo = Psicologo(10L, usuarioPsi, "COL-1", mutableListOf("Clínica"), null)
        return Paciente(20L, usuarioPac, psicologo)
    }

    private fun servicioConReloj(reloj: Clock = relojFijo()) =
        ServicioCita(citaRepository, pacienteRepository, reloj)

    @Test
    fun `obtenerDisponibilidadDia lanza en fin de semana`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)

        assertThrows<IllegalStateException> {
            servicio.obtenerDisponibilidadDia("uid-pac", sabado, zona)
        }
    }

    @Test
    fun `obtenerDisponibilidadDia devuelve slots en dia laborable sin ocupacion`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(
            citaRepository.findByPsicologoIdAndInicioEnRangoAndEstado(
                psicologoId = eq(10L),
                inicioDesde = any(),
                inicioHasta = any(),
                estado = eq(EstadoCita.RESERVADA),
            ),
        ).thenReturn(emptyList())

        val resultado = servicio.obtenerDisponibilidadDia("uid-pac", martesLaborable, zona)

        assertEquals(martesLaborable, resultado.fecha)
        assertEquals(zona, resultado.zonaHoraria)
        assertEquals((9..16).map { LocalTime.of(it, 0) }, resultado.horasDisponibles)
    }

    @Test
    fun `obtenerDisponibilidadDia excluye horas con cita reservada`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        val inicioOcupado = ZonedDateTime.of(martesLaborable, LocalTime.of(10, 0), ZoneId.of(zona)).toInstant()
        val citaOcupada = Cita(
            id = 1L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = inicioOcupado,
            estado = EstadoCita.RESERVADA,
        )
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(
            citaRepository.findByPsicologoIdAndInicioEnRangoAndEstado(
                psicologoId = eq(10L),
                inicioDesde = any(),
                inicioHasta = any(),
                estado = eq(EstadoCita.RESERVADA),
            ),
        ).thenReturn(listOf(citaOcupada))

        val resultado = servicio.obtenerDisponibilidadDia("uid-pac", martesLaborable, zona)

        assertFalse(resultado.horasDisponibles.contains(LocalTime.of(10, 0)))
        assertTrue(resultado.horasDisponibles.contains(LocalTime.of(9, 0)))
    }

    @Test
    fun `reservarCita crea cita nueva en slot libre`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        val inicio = ZonedDateTime.of(martesLaborable, LocalTime.of(11, 0), ZoneId.of(zona)).toOffsetDateTime()
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(
            citaRepository.findByPsicologoIdAndInicioEnRango(
                psicologoId = eq(10L),
                inicioDesde = any(),
                inicioHasta = any(),
            ),
        ).thenReturn(emptyList())
        whenever(citaRepository.save(any<Cita>())).thenAnswer { invocation ->
            val cita = invocation.getArgument<Cita>(0)
            cita.id = 99L
            cita
        }

        val resultado = servicio.reservarCita(
            "uid-pac",
            CitaCrearRequest(inicio = inicio, zonaHoraria = zona),
        )

        assertEquals(99L, resultado.id)
        assertEquals(EstadoCita.RESERVADA, resultado.estadoPersistido)
        verify(citaRepository).save(any())
    }

    @Test
    fun `reservarCita lanza conflicto cuando el slot ya esta reservado`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        val inicio = ZonedDateTime.of(martesLaborable, LocalTime.of(11, 0), ZoneId.of(zona)).toOffsetDateTime()
        val existente = Cita(
            id = 5L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = inicio.toInstant(),
            estado = EstadoCita.RESERVADA,
        )
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(
            citaRepository.findByPsicologoIdAndInicioEnRango(
                psicologoId = eq(10L),
                inicioDesde = any(),
                inicioHasta = any(),
            ),
        ).thenReturn(listOf(existente))

        val ex = assertThrows<IllegalStateException> {
            servicio.reservarCita("uid-pac", CitaCrearRequest(inicio = inicio, zonaHoraria = zona))
        }
        assertEquals("CONFLICTO_CITA_SLOT", ex.message)
    }

    @Test
    fun `reservarCita reutiliza cita cancelada en el mismo slot`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        val inicio = ZonedDateTime.of(martesLaborable, LocalTime.of(14, 0), ZoneId.of(zona)).toOffsetDateTime()
        val cancelada = Cita(
            id = 7L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = inicio.toInstant(),
            estado = EstadoCita.CANCELADA,
        )
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(paciente)
        whenever(
            citaRepository.findByPsicologoIdAndInicioEnRango(
                psicologoId = eq(10L),
                inicioDesde = any(),
                inicioHasta = any(),
            ),
        ).thenReturn(listOf(cancelada))
        whenever(citaRepository.save(cancelada)).thenReturn(cancelada)

        val resultado = servicio.reservarCita(
            "uid-pac",
            CitaCrearRequest(inicio = inicio, zonaHoraria = zona),
        )

        assertEquals(EstadoCita.RESERVADA, cancelada.estado)
        assertEquals(7L, resultado.id)
        verify(citaRepository).save(cancelada)
    }

    @Test
    fun `cancelarCita marca como cancelada cuando la cita esta activa`() {
        val inicio = Instant.parse("2025-05-27T10:00:00Z")
        val reloj = relojFijo(Instant.parse("2025-05-27T10:30:00Z"))
        val servicio = servicioConReloj(reloj)
        val paciente = pacienteConPsicologo()
        val cita = Cita(
            id = 3L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = inicio,
            estado = EstadoCita.RESERVADA,
        )
        whenever(citaRepository.findById(3L)).thenReturn(Optional.of(cita))
        whenever(citaRepository.save(cita)).thenReturn(cita)

        val resultado = servicio.cancelarCita("uid-pac", 3L)

        assertEquals(EstadoCita.CANCELADA, cita.estado)
        assertEquals(EstadoCitaCalculado.CANCELADA, resultado.estadoCalculado)
    }

    @Test
    fun `cancelarCita lanza SecurityException cuando el paciente no es el dueno`() {
        val servicio = servicioConReloj()
        val paciente = pacienteConPsicologo()
        val cita = Cita(
            id = 3L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = Instant.parse("2025-05-27T10:00:00Z"),
            estado = EstadoCita.RESERVADA,
        )
        whenever(citaRepository.findById(3L)).thenReturn(Optional.of(cita))

        assertThrows<SecurityException> {
            servicio.cancelarCita("uid-otro", 3L)
        }
    }

    @Test
    fun `cancelarCita lanza cuando la cita ya finalizo`() {
        val inicio = Instant.parse("2025-05-27T09:00:00Z")
        val reloj = relojFijo(Instant.parse("2025-05-27T11:00:00Z"))
        val servicio = servicioConReloj(reloj)
        val paciente = pacienteConPsicologo()
        val cita = Cita(
            id = 3L,
            psicologo = paciente.psicologo!!,
            paciente = paciente,
            inicio = inicio,
            estado = EstadoCita.RESERVADA,
        )
        whenever(citaRepository.findById(3L)).thenReturn(Optional.of(cita))

        assertThrows<IllegalStateException> {
            servicio.cancelarCita("uid-pac", 3L)
        }
    }
}
