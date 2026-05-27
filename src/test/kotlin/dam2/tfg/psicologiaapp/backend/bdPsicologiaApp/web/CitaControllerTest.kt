package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.DisponibilidadResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.EstadoCitaCalculado
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class CitaControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioCita: IServicioCita

    private val pacienteUser = FirebaseUserData("uid-pac", "pac@b.com")
    private val psicologoUser = FirebaseUserData("uid-psi", "psi@b.com")

    private val psicologoResp = PsicologoResponse(
        id = 1L, idEntidadPsicologo = 10L, firebaseUid = "uid-psi",
        nombre = "Psi", apellidos = "Apellidos", fotoPerfilUrl = null,
        numeroColegiado = "123", especialidades = listOf("Clinica"), descripcion = null,
    )
    private val pacienteResp = PacienteResponse(
        id = 2L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos",
        fotoPerfilUrl = null, psicologoId = 10L, idPaciente = 20L,
    )
    private val citaResponse = CitaResponse(
        id = 1L,
        inicio = OffsetDateTime.of(2026, 5, 27, 10, 0, 0, 0, ZoneOffset.UTC),
        fin = OffsetDateTime.of(2026, 5, 27, 11, 0, 0, 0, ZoneOffset.UTC),
        psicologo = psicologoResp,
        paciente = pacienteResp,
        estadoPersistido = EstadoCita.RESERVADA,
        estadoCalculado = EstadoCitaCalculado.ACTIVA,
    )

    @BeforeEach
    fun setUp() {
        servicioCita = mock()
        mockMvc = MockMvcBuilders.standaloneSetup(CitaController(servicioCita))
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withPacienteUser() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        SecurityContextHelper.setAuth(pacienteUser, "ROLE_PACIENTE")
        request
    }

    private fun withPsicologoUser() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        SecurityContextHelper.setAuth(psicologoUser, "ROLE_PSICOLOGO")
        request
    }

    @Test
    fun `GET disponibilidad devuelve 200 cuando hay horas`() {
        val respuesta = DisponibilidadResponse(
            fecha = LocalDate.of(2026, 5, 27),
            zonaHoraria = "Europe/Madrid",
            horasDisponibles = listOf(LocalTime.of(10, 0)),
        )
        whenever(servicioCita.obtenerDisponibilidadDia("uid-pac", LocalDate.of(2026, 5, 27), "Europe/Madrid"))
            .thenReturn(respuesta)

        mockMvc.perform(
            get("/api/citas/disponibilidad")
                .param("fecha", "2026-05-27")
                .param("zonaHoraria", "Europe/Madrid")
                .with(withPacienteUser())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.horasDisponibles.length()").value(1))

        verify(servicioCita).obtenerDisponibilidadDia("uid-pac", LocalDate.of(2026, 5, 27), "Europe/Madrid")
    }

    @Test
    fun `GET disponibilidad devuelve 204 cuando no hay horas`() {
        val respuesta = DisponibilidadResponse(
            fecha = LocalDate.of(2026, 5, 27),
            zonaHoraria = "Europe/Madrid",
            horasDisponibles = emptyList(),
        )
        whenever(servicioCita.obtenerDisponibilidadDia(any(), any(), any())).thenReturn(respuesta)

        mockMvc.perform(
            get("/api/citas/disponibilidad")
                .param("fecha", "2026-05-27")
                .param("zonaHoraria", "Europe/Madrid")
                .with(withPacienteUser())
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `GET disponibilidad devuelve 400 cuando el servicio lanza IllegalStateException`() {
        whenever(servicioCita.obtenerDisponibilidadDia(any(), any(), any()))
            .thenThrow(IllegalStateException("Sin psicólogo"))

        mockMvc.perform(
            get("/api/citas/disponibilidad")
                .param("fecha", "2026-05-27")
                .param("zonaHoraria", "Europe/Madrid")
                .with(withPacienteUser())
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `POST api citas reserva y devuelve 201`() {
        whenever(servicioCita.reservarCita(eq("uid-pac"), any())).thenReturn(citaResponse)

        mockMvc.perform(
            post("/api/citas")
                .with(withPacienteUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inicio":"2026-05-27T10:00:00+00:00","zonaHoraria":"Europe/Madrid"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))

        verify(servicioCita).reservarCita(eq("uid-pac"), any())
    }

    @Test
    fun `POST api citas devuelve 409 cuando hay conflicto de slot`() {
        whenever(servicioCita.reservarCita(any(), any()))
            .thenThrow(IllegalStateException("CONFLICTO_CITA_SLOT"))

        mockMvc.perform(
            post("/api/citas")
                .with(withPacienteUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"inicio":"2026-05-27T10:00:00+00:00","zonaHoraria":"Europe/Madrid"}""")
        ).andExpect(status().isConflict)
    }

    @Test
    fun `GET api citas devuelve 200 con citas del paciente`() {
        whenever(servicioCita.obtenerMisCitasPaciente("uid-pac")).thenReturn(listOf(citaResponse))

        mockMvc.perform(get("/api/citas").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))

        verify(servicioCita).obtenerMisCitasPaciente("uid-pac")
    }

    @Test
    fun `GET api citas psicologo devuelve 204 cuando no hay citas`() {
        whenever(servicioCita.obtenerMisCitasPsicologo("uid-psi")).thenReturn(emptyList())

        mockMvc.perform(get("/api/citas/psicologo").with(withPsicologoUser()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `PATCH cancelar devuelve 200 cuando ok`() {
        whenever(servicioCita.cancelarCita("uid-pac", 1L)).thenReturn(citaResponse)

        mockMvc.perform(patch("/api/citas/1/cancelar").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))

        verify(servicioCita).cancelarCita("uid-pac", 1L)
    }
}

private object SecurityContextHelper {
    fun setAuth(user: FirebaseUserData, role: String) {
        val auth = UsernamePasswordAuthenticationToken(
            user, null, listOf(SimpleGrantedAuthority(role))
        )
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
    }
}
