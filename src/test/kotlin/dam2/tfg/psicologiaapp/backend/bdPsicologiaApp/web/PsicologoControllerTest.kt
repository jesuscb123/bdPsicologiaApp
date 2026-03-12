package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
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

internal class PsicologoControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioPsicologo: IServicioPsicologo
    private lateinit var servicioUsuario: IServicioUsuario

    private val firebaseUser = FirebaseUserData("uid-paciente", "pac@b.com")

    @BeforeEach
    fun setUp() {
        servicioPsicologo = mock()
        servicioUsuario = mock()
        val controller = PsicologoController(servicioPsicologo, servicioUsuario)
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
    fun `GET api psicologos buscar devuelve 200 y lista`() {
        whenever(servicioPsicologo.buscarPsicologosPorNombre("nombre")).thenReturn(emptyList())

        mockMvc.perform(get("/api/psicologos/buscar").param("nombreUsuario", "nombre").with(withPacienteUser()))
            .andExpect(status().isOk)

        verify(servicioPsicologo).buscarPsicologosPorNombre("nombre")
    }

    @Test
    fun `GET api psicologos devuelve 200`() {
        whenever(servicioPsicologo.obtenerPsicologos()).thenReturn(emptyList())

        mockMvc.perform(get("/api/psicologos").with(withPacienteUser()))
            .andExpect(status().isOk)

        verify(servicioPsicologo).obtenerPsicologos()
    }
}
