package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioTarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse
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
import java.time.LocalDateTime

internal class TareaControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioTarea: IServicioTarea

    private val firebaseUser = FirebaseUserData("uid-psi", "psi@b.com")

    @BeforeEach
    fun setUp() {
        servicioTarea = mock()
        val controller = TareaController(servicioTarea)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
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

    @Test
    fun `PUT api tareas id actualiza y devuelve 200`() {
        val psicologoResp = PsicologoResponse(id = 1L, firebaseUid = "uid-psi", nombreUsuario = "psi", fotoPerfilUrl = null, numeroColegiado = "123", especialidad = "Esp")
        val pacienteResp = PacienteResponse(id = 1L, firebaseUid = "uid-pac", nombreUsuario = "pac", fotoPerfilUrl = null, psicologoId = null)
        val tareaResponse = TareaResponse(1L, "Titulo nuevo", "Desc nueva", LocalDateTime.now(), false, psicologoResp, pacienteResp)
        whenever(servicioTarea.actualizarTarea(eq("uid-psi"), eq(1L), any())).thenReturn(tareaResponse)

        mockMvc.perform(
            put("/api/tareas/1")
                .with(withPsicologoUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"titulo":"Titulo nuevo","descripcion":"Desc nueva"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.titulo").value("Titulo nuevo"))

        verify(servicioTarea).actualizarTarea(eq("uid-psi"), eq(1L), argThat { req: TareaActualizarRequest -> req.titulo == "Titulo nuevo" && req.descripcion == "Desc nueva" })
    }

    @Test
    fun `DELETE api tareas id devuelve 204 cuando ok`() {
        doNothing().whenever(servicioTarea).eliminarTarea("uid-psi", 1L)

        mockMvc.perform(delete("/api/tareas/1").with(withPsicologoUser()))
            .andExpect(status().isNoContent)

        verify(servicioTarea).eliminarTarea("uid-psi", 1L)
    }
}
