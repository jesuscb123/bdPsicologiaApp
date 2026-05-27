package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioFcmToken
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class NotificacionesControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioFcmToken: IServicioFcmToken

    private val firebaseUser = FirebaseUserData("uid1", "a@b.com")

    @BeforeEach
    fun setUp() {
        servicioFcmToken = mock()
        mockMvc = MockMvcBuilders.standaloneSetup(NotificacionesController(servicioFcmToken))
            .setControllerAdvice(GlobalExceptionHandler())
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withFirebaseUser() = org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
        val auth = UsernamePasswordAuthenticationToken(
            firebaseUser, null, listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
        org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
        request
    }

    @Test
    fun `POST fcm token devuelve 204 cuando ok`() {
        doNothing().whenever(servicioFcmToken).registrarToken(
            eq("uid1"), eq("token-fcm"), eq("inst-1"), eq("ANDROID")
        )

        mockMvc.perform(
            post("/api/notificaciones/fcm/token")
                .with(withFirebaseUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"token-fcm","instalacionId":"inst-1","plataforma":"ANDROID"}""")
        ).andExpect(status().isNoContent)

        verify(servicioFcmToken).registrarToken("uid1", "token-fcm", "inst-1", "ANDROID")
    }

    @Test
    fun `POST fcm token devuelve 400 cuando argumento invalido`() {
        doThrow(IllegalArgumentException("Token inválido"))
            .whenever(servicioFcmToken)
            .registrarToken(eq("uid1"), eq("bad"), isNull(), isNull())

        mockMvc.perform(
            post("/api/notificaciones/fcm/token")
                .with(withFirebaseUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"bad"}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `POST fcm token baja devuelve 204 cuando ok`() {
        doNothing().whenever(servicioFcmToken).eliminarToken("uid1", "token-fcm")

        mockMvc.perform(
            post("/api/notificaciones/fcm/token/baja")
                .with(withFirebaseUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"token":"token-fcm"}""")
        ).andExpect(status().isNoContent)

        verify(servicioFcmToken).eliminarToken("uid1", "token-fcm")
    }
}
