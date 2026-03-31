package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
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
import org.springframework.http.MediaType

internal class PacienteControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var servicioPaciente: IServicioPaciente
    private lateinit var usuarioRepository: UsuarioRepository

    private val firebaseUser = FirebaseUserData("uid-psi", "psi@b.com")

    @BeforeEach
    fun setUp() {
        servicioPaciente = mock()
        usuarioRepository = mock()
        val controller = PacienteController(servicioPaciente, usuarioRepository)
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

    @Test
    fun `POST api pacientes me devuelve 409 si usuario no existe`() {
        whenever(usuarioRepository.findByFirebaseUid(firebaseUser.uid)).thenReturn(null)

        mockMvc.perform(
            post("/api/pacientes/me")
                .with(withPsicologoUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"psicologoId": 1}""")
        ).andExpect(status().isConflict)

        verify(usuarioRepository).findByFirebaseUid(firebaseUser.uid)
        verifyNoInteractions(servicioPaciente)
    }

    @Test
    fun `POST api pacientes me devuelve 201 si crea paciente`() {
        val usuario = Usuario(
            id = 10L,
            firebaseUid = firebaseUser.uid,
            email = firebaseUser.email,
            nombreUsuario = "nombre",
            fotoPerfilUrl = null
        )
        whenever(usuarioRepository.findByFirebaseUid(firebaseUser.uid)).thenReturn(usuario)
        whenever(servicioPaciente.crearPaciente(eq(usuario), any())).thenReturn(
            PacienteResponse(
                id = 1L,
                firebaseUid = firebaseUser.uid,
                nombreUsuario = "nombre",
                fotoPerfilUrl = null,
                psicologoId = 2L,
                idPaciente = 1L
            )
        )

        mockMvc.perform(
            post("/api/pacientes/me")
                .with(withPsicologoUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"psicologoId": 2}""")
        ).andExpect(status().isCreated)

        verify(usuarioRepository).findByFirebaseUid(firebaseUser.uid)
        verify(servicioPaciente).crearPaciente(eq(usuario), any())
    }
}
