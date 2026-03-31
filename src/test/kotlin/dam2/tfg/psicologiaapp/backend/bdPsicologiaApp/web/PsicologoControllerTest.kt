package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
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
    private lateinit var usuarioRepository: UsuarioRepository

    private val firebaseUser = FirebaseUserData("uid-paciente", "pac@b.com")

    @BeforeEach
    fun setUp() {
        servicioPsicologo = mock()
        usuarioRepository = mock()
        val controller = PsicologoController(servicioPsicologo, usuarioRepository)
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

    @Test
    fun `POST api psicologos me devuelve 201 si usuario existe`() {
        val usuario = Usuario(
            id = 1L,
            firebaseUid = firebaseUser.uid,
            email = firebaseUser.email,
            nombreUsuario = "nombre",
            fotoPerfilUrl = null
        )
        whenever(usuarioRepository.findByFirebaseUid(firebaseUser.uid)).thenReturn(usuario)
        whenever(servicioPsicologo.crearPsicologo(eq(usuario), any())).thenReturn(
            PsicologoResponse(
                id = 10L,
                firebaseUid = firebaseUser.uid,
                nombreUsuario = "nombre",
                fotoPerfilUrl = null,
                numeroColegiado = "1234",
                especialidad = "clinica"
            )
        )

        mockMvc.perform(
            post("/api/psicologos/me")
                .with(withPacienteUser())
                .contentType("application/json")
                .content("""{"numeroColegiado":"1234","especialidad":"clinica"}""")
        ).andExpect(status().isCreated)

        verify(usuarioRepository).findByFirebaseUid(firebaseUser.uid)
        verify(servicioPsicologo).crearPsicologo(eq(usuario), any())
    }

    @Test
    fun `POST api psicologos me devuelve 409 si usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid(firebaseUser.uid)).thenReturn(null)

        mockMvc.perform(
            post("/api/psicologos/me")
                .with(withPacienteUser())
                .contentType("application/json")
                .content("""{"numeroColegiado":"1234","especialidad":"clinica"}""")
        )
            .andExpect(status().isConflict)

        verify(usuarioRepository).findByFirebaseUid(firebaseUser.uid)
        verifyNoInteractions(servicioPsicologo)
    }
}
