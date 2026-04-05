package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilBasicoResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.setup.MockMvcBuilders

internal class UsuarioControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioUsuario: IServicioUsuario

    private val firebaseUser = FirebaseUserData("uid1", "a@b.com")

    @BeforeEach
    fun setUp() {
        servicioUsuario = mock()
        val controller = UsuarioController(servicioUsuario)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(FirebaseUserArgumentResolver())
            .build()
    }

    private fun withFirebaseUser(): org.springframework.test.web.servlet.request.RequestPostProcessor {
        val auth = UsernamePasswordAuthenticationToken(firebaseUser, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
        return org.springframework.test.web.servlet.request.RequestPostProcessor { request ->
            org.springframework.security.core.context.SecurityContextHolder.getContext().authentication = auth
            request
        }
    }

    @Test
    fun `GET api usuarios devuelve 200 y lista`() {
        whenever(servicioUsuario.obtenerUsuarios()).thenReturn(emptyList())

        mockMvc.perform(get("/api/usuarios").with(withFirebaseUser()))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        verify(servicioUsuario).obtenerUsuarios()
    }

    @Test
    fun `GET api usuarios me devuelve 200 y perfil`() {
        val perfil = UsuarioPerfilBasicoResponse(1L, "uid1", "nombre", "a@b.com", null)
        whenever(servicioUsuario.obtenerPerfilUsuario("uid1")).thenReturn(perfil)

        mockMvc.perform(get("/api/usuarios/me").with(withFirebaseUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firebaseUid").value("uid1"))
            .andExpect(jsonPath("$.email").value("a@b.com"))

        verify(servicioUsuario).obtenerPerfilUsuario("uid1")
    }

    @Test
    fun `PATCH api usuarios me email devuelve 200 cuando ok`() {
        val perfil = UsuarioPerfilBasicoResponse(1L, "uid1", "nombre", "nuevo@b.com", null)
        whenever(servicioUsuario.actualizarEmailUsuario(eq("uid1"), eq("nuevo@b.com"))).thenReturn(perfil)

        mockMvc.perform(
            patch("/api/usuarios/me/email")
                .with(withFirebaseUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nuevoEmail":"nuevo@b.com"}""")
        )
            .andExpect(status().isOk)

        verify(servicioUsuario).actualizarEmailUsuario("uid1", "nuevo@b.com")
    }

    @Test
    fun `POST api usuarios me foto devuelve 200 cuando ok`() {
        val bytes = byteArrayOf(1, 2, 3)
        val perfil = UsuarioPerfilBasicoResponse(
            1L,
            "uid1",
            "nombre",
            "a@b.com",
            "https://api.example.com/api/archivos/perfiles/abc.jpg"
        )
        whenever(
            servicioUsuario.subirFotoPerfilDesdeArchivo(
                eq("uid1"),
                eq(bytes),
                eq("image/jpeg"),
            )
        ).thenReturn(perfil)

        val archivo = MockMultipartFile("archivo", "foto.jpg", "image/jpeg", bytes)

        mockMvc.perform(
            multipart("/api/usuarios/me/foto")
                .file(archivo)
                .with(withFirebaseUser())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fotoPerfilUrl").value("https://api.example.com/api/archivos/perfiles/abc.jpg"))

        verify(servicioUsuario).subirFotoPerfilDesdeArchivo("uid1", bytes, "image/jpeg")
    }

    @Test
    fun `DELETE api usuarios me devuelve 204 cuando ok`() {
        doNothing().whenever(servicioUsuario).eliminarUsuario("uid1")

        mockMvc.perform(delete("/api/usuarios/me").with(withFirebaseUser()))
            .andExpect(status().isNoContent)

        verify(servicioUsuario).eliminarUsuario("uid1")
    }
}
