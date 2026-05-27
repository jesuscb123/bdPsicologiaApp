package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioChat
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO.ChatResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class ChatControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioChat: IServicioChat

    private val pacienteUser = FirebaseUserData("uid-pac", "pac@test.com")
    private val psicologoUser = FirebaseUserData("uid-psi", "psi@test.com")

    private val chatResponseEjemplo = ChatResponse(
        chatId = "paciente_20_psicologo_10",
        interlocutorNombre = "Interlocutor",
        interlocutorApellidos = "Apellidos",
        interlocutorFotoPerfilUrl = null,
        rtdbRuta = "chats/paciente_20_psicologo_10"
    )

    @BeforeEach
    fun setUp() {
        servicioChat = mock()
        val controller = ChatController(servicioChat)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withPacienteAuth() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        val auth = UsernamePasswordAuthenticationToken(
            pacienteUser, null,
            listOf(SimpleGrantedAuthority("ROLE_PACIENTE"))
        )
        SecurityContextHolder.getContext().authentication = auth
        request
    }

    private fun withPsicologoAuth() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        val auth = UsernamePasswordAuthenticationToken(
            psicologoUser, null,
            listOf(SimpleGrantedAuthority("ROLE_PSICOLOGO"))
        )
        SecurityContextHolder.getContext().authentication = auth
        request
    }

    // ── POST /api/chats/me/psicologo ─────────────────────────────────────────

    @Test
    fun `POST me psicologo devuelve 200 cuando el servicio tiene exito`() {
        whenever(servicioChat.asegurarChatPaciente(pacienteUser.uid)).thenReturn(chatResponseEjemplo)

        mockMvc.perform(post("/api/chats/me/psicologo").with(withPacienteAuth()))
            .andExpect(status().isOk)

        verify(servicioChat).asegurarChatPaciente(pacienteUser.uid)
    }

    @Test
    fun `POST me psicologo devuelve 400 cuando el paciente no tiene psicologo asignado`() {
        whenever(servicioChat.asegurarChatPaciente(pacienteUser.uid))
            .thenThrow(IllegalStateException("El paciente no tiene psicólogo asignado"))

        mockMvc.perform(post("/api/chats/me/psicologo").with(withPacienteAuth()))
            .andExpect(status().isBadRequest)
    }

    // ── POST /api/chats/pacientes/{pacienteId} ───────────────────────────────

    @Test
    fun `POST pacientes id devuelve 200 cuando el paciente esta asignado al psicologo`() {
        whenever(servicioChat.asegurarChatPsicologo(psicologoUser.uid, 20L))
            .thenReturn(chatResponseEjemplo)

        mockMvc.perform(post("/api/chats/pacientes/20").with(withPsicologoAuth()))
            .andExpect(status().isOk)

        verify(servicioChat).asegurarChatPsicologo(psicologoUser.uid, 20L)
    }

    @Test
    fun `POST pacientes id devuelve 403 cuando el paciente no esta asignado al psicologo`() {
        whenever(servicioChat.asegurarChatPsicologo(psicologoUser.uid, 20L))
            .thenThrow(SecurityException("El paciente con id 20 no está asignado a este psicólogo"))

        mockMvc.perform(post("/api/chats/pacientes/20").with(withPsicologoAuth()))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `POST pacientes id devuelve 400 cuando el psicologo no existe`() {
        whenever(servicioChat.asegurarChatPsicologo(psicologoUser.uid, 20L))
            .thenThrow(IllegalStateException("Psicólogo no encontrado"))

        mockMvc.perform(post("/api/chats/pacientes/20").with(withPsicologoAuth()))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST notificar devuelve 204 cuando ok`() {
        doNothing().whenever(servicioChat).notificarMensajeChat(
            eq(pacienteUser.uid), eq("chat-1"), eq("Hola")
        )

        mockMvc.perform(
            post("/api/chats/notificar")
                .with(withPacienteAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"chatId":"chat-1","vistaPreviaTexto":"Hola"}""")
        ).andExpect(status().isNoContent)

        verify(servicioChat).notificarMensajeChat(pacienteUser.uid, "chat-1", "Hola")
    }

    @Test
    fun `POST notificar devuelve 403 cuando acceso denegado`() {
        whenever(
            servicioChat.notificarMensajeChat(any(), any(), any())
        ).thenThrow(SecurityException("No pertenece al chat"))

        mockMvc.perform(
            post("/api/chats/notificar")
                .with(withPacienteAuth())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"chatId":"chat-1","vistaPreviaTexto":"Hola"}""")
        ).andExpect(status().isForbidden)
    }
}
