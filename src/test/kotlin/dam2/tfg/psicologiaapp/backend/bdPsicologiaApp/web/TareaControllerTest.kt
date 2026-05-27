package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioTarea
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.EstadoSyncResponse
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
    private val pacienteUser = FirebaseUserData("uid-pac", "pac@b.com")

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

    private fun withPacienteUser(): org.springframework.test.web.servlet.request.RequestPostProcessor {
        val auth = UsernamePasswordAuthenticationToken(
            pacienteUser,
            null,
            listOf(SimpleGrantedAuthority("ROLE_PACIENTE"))
        )
        return org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
            org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
            request
        }
    }

    private fun tareaEjemplo(): TareaResponse {
        val psicologoResp = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 1L, firebaseUid = "uid-psi", nombre = "Psi",
            apellidos = "Apellidos", fotoPerfilUrl = null, numeroColegiado = "123",
            especialidades = listOf("Esp"), descripcion = null,
        )
        val pacienteResp = PacienteResponse(
            id = 1L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos",
            fotoPerfilUrl = null, psicologoId = null, idPaciente = 1L,
        )
        return TareaResponse(1L, "Titulo nuevo", "Desc nueva", LocalDateTime.now(), false, false, psicologoResp, pacienteResp)
    }

    @Test
    fun `PUT api tareas id actualiza y devuelve 200`() {
        val psicologoResp = PsicologoResponse(id = 1L, idEntidadPsicologo = 1L, firebaseUid = "uid-psi", nombre = "Psi", apellidos = "Apellidos", fotoPerfilUrl = null, numeroColegiado = "123", especialidades = listOf("Esp"), descripcion = null)
        val pacienteResp = PacienteResponse(id = 1L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos", fotoPerfilUrl = null, psicologoId = null, idPaciente = 1L)
        val tareaResponse = TareaResponse(1L, "Titulo nuevo", "Desc nueva", LocalDateTime.now(), false, false, psicologoResp, pacienteResp)
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

    @Test
    fun `GET api tareas devuelve 200 con tareas del paciente`() {
        whenever(servicioTarea.obtenerTareasPaciente("uid-pac")).thenReturn(listOf(tareaEjemplo()))

        mockMvc.perform(get("/api/tareas").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].titulo").value("Titulo nuevo"))
    }

    @Test
    fun `GET api tareas estado devuelve 200`() {
        val estado = EstadoSyncResponse(LocalDateTime.now(), 2L)
        whenever(servicioTarea.obtenerEstadoTareasPaciente("uid-pac")).thenReturn(estado)

        mockMvc.perform(get("/api/tareas/estado").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(2))
    }

    @Test
    fun `GET api tareas pacientes id devuelve 200 para psicologo`() {
        whenever(servicioTarea.obtenerTareasPacienteParaPsicologo("uid-psi", 20L))
            .thenReturn(listOf(tareaEjemplo()))

        mockMvc.perform(get("/api/tareas/pacientes/20").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(1))
    }

    @Test
    fun `GET api tareas pacientes id estado devuelve 200 para psicologo`() {
        val estado = EstadoSyncResponse(LocalDateTime.now(), 1L)
        whenever(servicioTarea.obtenerEstadoTareasPacienteParaPsicologo("uid-psi", 20L))
            .thenReturn(estado)

        mockMvc.perform(get("/api/tareas/pacientes/20/estado").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.total").value(1))
    }

    @Test
    fun `POST api tareas pacientes id crea tarea y devuelve 201`() {
        whenever(servicioTarea.crearTarea(eq("uid-psi"), eq(20L), any())).thenReturn(tareaEjemplo())

        mockMvc.perform(
            post("/api/tareas/pacientes/20")
                .with(withPsicologoUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"titulo":"Titulo nuevo","descripcion":"Desc nueva"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.titulo").value("Titulo nuevo"))
    }

    @Test
    fun `PATCH api tareas id realizada devuelve 200`() {
        val tarea = tareaEjemplo().copy(realizada = true)
        whenever(servicioTarea.actualizarRealizada(eq("uid-pac"), eq(1L), any())).thenReturn(tarea)

        mockMvc.perform(
            patch("/api/tareas/1/realizada")
                .with(withPacienteUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"realizada":true}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.realizada").value(true))
    }

    @Test
    fun `PATCH api tareas id aceptada devuelve 200`() {
        val tarea = tareaEjemplo().copy(aceptadaPorPaciente = true)
        whenever(servicioTarea.aceptarTareaPaciente("uid-pac", 1L)).thenReturn(tarea)

        mockMvc.perform(patch("/api/tareas/1/aceptada").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.aceptadaPorPaciente").value(true))
    }
}
