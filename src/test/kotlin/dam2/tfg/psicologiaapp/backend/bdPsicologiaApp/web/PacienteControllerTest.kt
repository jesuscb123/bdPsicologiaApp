package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPaciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class PacienteControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioPaciente: IServicioPaciente

    private val firebaseUser = FirebaseUserData("uid-psi", "psi@b.com")

    @BeforeEach
    fun setUp() {
        servicioPaciente = mock()
        val controller = PacienteController(servicioPaciente)
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
    fun `GET api pacientes buscar devuelve 200 y lista`() {
        whenever(servicioPaciente.buscarPacientesPorNombre("nombre")).thenReturn(emptyList())

        mockMvc.perform(get("/api/pacientes/buscar").param("nombreUsuario", "nombre").with(withPsicologoUser()))
            .andExpect(status().isOk)

        verify(servicioPaciente).buscarPacientesPorNombre("nombre")
    }

    @Test
    fun `GET api pacientes devuelve 200`() {
        whenever(servicioPaciente.obtenerPacientes()).thenReturn(emptyList())

        mockMvc.perform(get("/api/pacientes").with(withPsicologoUser()))
            .andExpect(status().isOk)

        verify(servicioPaciente).obtenerPacientes()
    }
}
