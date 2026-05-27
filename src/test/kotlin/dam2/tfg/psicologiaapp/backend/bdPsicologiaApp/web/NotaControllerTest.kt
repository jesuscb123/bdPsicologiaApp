package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.EstadoSyncResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class NotaControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioNota: IServicioNota

    private val firebaseUser = FirebaseUserData("uid-paciente", "a@b.com")

    @BeforeEach
    fun setUp() {
        servicioNota = mock()
        val controller = NotaController(servicioNota)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withPacienteUser(): org.springframework.test.web.servlet.request.RequestPostProcessor {
        val auth = UsernamePasswordAuthenticationToken(
            firebaseUser,
            null,
            listOf(SimpleGrantedAuthority("ROLE_PACIENTE"))
        )
        return org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
            org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
            request
        }
    }

    @Test
    fun `PUT api notas id actualiza y devuelve 200`() {
        val pacienteResp = PacienteResponse(id = 1L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos", fotoPerfilUrl = null, psicologoId = null, idPaciente = 1L)
        val psicologoResp = PsicologoResponse(id = 1L, idEntidadPsicologo = 1L, firebaseUid = "uid-psi", nombre = "Psi", apellidos = "Apellidos", fotoPerfilUrl = null, numeroColegiado = "123", especialidades = listOf("Esp"), descripcion = null)
        val notaResponse = NotaResponse(1L, "Asunto nuevo", "Desc nueva", LocalDateTime.now(), pacienteResp, psicologoResp)
        whenever(servicioNota.actualizarNota(eq("uid-paciente"), eq(1L), any())).thenReturn(notaResponse)

        mockMvc.perform(
            put("/api/notas/1")
                .with(withPacienteUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"asunto":"Asunto nuevo","descripcion":"Desc nueva"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.asunto").value("Asunto nuevo"))

        verify(servicioNota).actualizarNota(eq("uid-paciente"), eq(1L), argThat { asunto == "Asunto nuevo" && descripcion == "Desc nueva" })
    }

    @Test
    fun `DELETE api notas id devuelve 204 cuando ok`() {
        doNothing().whenever(servicioNota).eliminarNota("uid-paciente", 1L)

        mockMvc.perform(delete("/api/notas/1").with(withPacienteUser()))
            .andExpect(status().isNoContent)

        verify(servicioNota).eliminarNota("uid-paciente", 1L)
    }

    @Test
    fun `GET api notas devuelve 200 con notas del paciente`() {
        val nota = notaEjemplo()
        whenever(servicioNota.obtenerNotasPaciente("uid-paciente")).thenReturn(listOf(nota))

        mockMvc.perform(get("/api/notas").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].asunto").value("Asunto nuevo"))

        verify(servicioNota).obtenerNotasPaciente("uid-paciente")
    }

    @Test
    fun `GET api notas devuelve 204 cuando no hay notas`() {
        whenever(servicioNota.obtenerNotasPaciente("uid-paciente")).thenReturn(emptyList())

        mockMvc.perform(get("/api/notas").with(withPacienteUser()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `GET api notas estado devuelve 200`() {
        val estado = EstadoSyncResponse(LocalDateTime.of(2026, 5, 27, 10, 0), 3L)
        whenever(servicioNota.obtenerEstadoNotasPaciente("uid-paciente")).thenReturn(estado)

        mockMvc.perform(get("/api/notas/estado").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(3))

        verify(servicioNota).obtenerEstadoNotasPaciente("uid-paciente")
    }

    @Test
    fun `GET api notas pacientes id devuelve 200 para psicologo`() {
        val nota = notaEjemplo()
        whenever(servicioNota.obtenerNotasPacienteParaPsicologo("uid-paciente", 20L))
            .thenReturn(listOf(nota))

        mockMvc.perform(get("/api/notas/pacientes/20").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))

        verify(servicioNota).obtenerNotasPacienteParaPsicologo("uid-paciente", 20L)
    }

    @Test
    fun `GET api notas pacientes id estado devuelve 200 para psicologo`() {
        val estado = EstadoSyncResponse(LocalDateTime.of(2026, 5, 27, 10, 0), 2L)
        whenever(servicioNota.obtenerEstadoNotasPacienteParaPsicologo("uid-paciente", 20L))
            .thenReturn(estado)

        mockMvc.perform(get("/api/notas/pacientes/20/estado").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(2))
    }

    @Test
    fun `POST api notas crea nota y devuelve 201`() {
        val nota = notaEjemplo()
        whenever(servicioNota.crearNota(eq("uid-paciente"), any())).thenReturn(nota)

        mockMvc.perform(
            post("/api/notas")
                .with(withPacienteUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"asunto":"Asunto nuevo","descripcion":"Desc nueva"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.asunto").value("Asunto nuevo"))

        verify(servicioNota).crearNota(eq("uid-paciente"), any())
    }

    private fun withPsicologoUser(): org.springframework.test.web.servlet.request.RequestPostProcessor {
        val auth = UsernamePasswordAuthenticationToken(
            firebaseUser,
            null,
            listOf(SimpleGrantedAuthority("ROLE_PSICOLOGO"))
        )
        return org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
            org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
            request
        }
    }

    private fun notaEjemplo(): NotaResponse {
        val pacienteResp = PacienteResponse(
            id = 1L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos",
            fotoPerfilUrl = null, psicologoId = null, idPaciente = 1L,
        )
        val psicologoResp = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 1L, firebaseUid = "uid-psi", nombre = "Psi",
            apellidos = "Apellidos", fotoPerfilUrl = null, numeroColegiado = "123",
            especialidades = listOf("Esp"), descripcion = null,
        )
        return NotaResponse(1L, "Asunto nuevo", "Desc nueva", LocalDateTime.now(), pacienteResp, psicologoResp)
    }
}
