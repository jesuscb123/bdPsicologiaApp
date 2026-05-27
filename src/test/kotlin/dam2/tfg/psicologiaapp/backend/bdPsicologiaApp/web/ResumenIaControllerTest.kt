package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioResumenIa
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.ResumenIaServicioNoDisponibleException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.iaDTO.ResumenIaResponse
import java.time.LocalDateTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class ResumenIaControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioResumenIa: IServicioResumenIa

    private val psicologoUser = FirebaseUserData("uid-psi", "psi@b.com")

    @BeforeEach
    fun setUp() {
        servicioResumenIa = mock()
        mockMvc = MockMvcBuilders.standaloneSetup(ResumenIaController(servicioResumenIa))
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withPsicologoUser() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        val auth = UsernamePasswordAuthenticationToken(
            psicologoUser, null, listOf(SimpleGrantedAuthority("ROLE_PSICOLOGO"))
        )
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
        request
    }

    @Test
    fun `POST resumen-ia devuelve 200 con resumen`() {
        val resumen = ResumenIaResponse(
            resumen = "Resumen generado",
            numeroNotasAnalizadas = 3,
            generadoEn = LocalDateTime.of(2026, 5, 27, 12, 0),
            modelo = "llama",
        )
        whenever(servicioResumenIa.generarResumenNotasPaciente("uid-psi", 20L)).thenReturn(resumen)

        mockMvc.perform(post("/api/notas/pacientes/20/resumen-ia").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resumen").value("Resumen generado"))
            .andExpect(jsonPath("$.numeroNotasAnalizadas").value(3))

        verify(servicioResumenIa).generarResumenNotasPaciente("uid-psi", 20L)
    }

    @Test
    fun `POST resumen-ia devuelve 403 cuando paciente no pertenece al psicologo`() {
        whenever(servicioResumenIa.generarResumenNotasPaciente(any(), any()))
            .thenThrow(SecurityException("Acceso denegado"))

        mockMvc.perform(post("/api/notas/pacientes/20/resumen-ia").with(withPsicologoUser()))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `POST resumen-ia devuelve 503 cuando servicio IA no disponible`() {
        whenever(servicioResumenIa.generarResumenNotasPaciente(any(), any()))
            .thenThrow(ResumenIaServicioNoDisponibleException("Servicio no disponible"))

        mockMvc.perform(post("/api/notas/pacientes/20/resumen-ia").with(withPsicologoUser()))
            .andExpect(status().isServiceUnavailable)
    }

    @Test
    fun `POST resumen-ia devuelve 404 cuando sin notas`() {
        whenever(servicioResumenIa.generarResumenNotasPaciente(any(), any()))
            .thenThrow(IllegalStateException("Sin notas"))

        mockMvc.perform(post("/api/notas/pacientes/20/resumen-ia").with(withPsicologoUser()))
            .andExpect(status().isNotFound)
    }
}
