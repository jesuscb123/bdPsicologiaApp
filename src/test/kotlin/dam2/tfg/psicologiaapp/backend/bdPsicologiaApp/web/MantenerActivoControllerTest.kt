package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class MantenerActivoControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioUsuario: IServicioUsuario

    @BeforeEach
    fun setUp() {
        servicioUsuario = mock()
        mockMvc = MockMvcBuilders.standaloneSetup(MantenerActivoController(servicioUsuario)).build()
    }

    @Test
    fun `GET mantener-activo devuelve 200 y status ok`() {
        whenever(servicioUsuario.existeCorreo("ping@mantener-activo.internal")).thenReturn(false)

        mockMvc.perform(get("/api/mantener-activo"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("ok"))

        verify(servicioUsuario).existeCorreo("ping@mantener-activo.internal")
    }
}
