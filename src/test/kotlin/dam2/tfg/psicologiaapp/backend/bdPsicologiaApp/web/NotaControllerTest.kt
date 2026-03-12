package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
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
        val controller = NotaContoller(servicioNota)
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
        val pacienteResp = PacienteResponse(id = 1L, firebaseUid = "uid-pac", nombreUsuario = "pac", fotoPerfilUrl = null, psicologoId = null)
        val psicologoResp = PsicologoResponse(id = 1L, firebaseUid = "uid-psi", nombreUsuario = "psi", fotoPerfilUrl = null, numeroColegiado = "123", especialidad = "Esp")
        val notaResponse = NotaResponse(1L, "Asunto nuevo", "Desc nueva", pacienteResp, psicologoResp)
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
}
