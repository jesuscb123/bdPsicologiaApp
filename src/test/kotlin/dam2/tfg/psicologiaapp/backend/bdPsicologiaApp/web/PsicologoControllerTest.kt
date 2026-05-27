package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
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
    private val psicologoUser = FirebaseUserData("uid-psi", "psi@b.com")

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

    private fun withPsicologoUser(): org.springframework.test.web.servlet.request.RequestPostProcessor {
        val auth = UsernamePasswordAuthenticationToken(
            psicologoUser,
            null,
            listOf(SimpleGrantedAuthority("ROLE_PSICOLOGO"))
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
            nombre = "Nombre",
            apellidos = "Apellidos",
            fotoPerfilUrl = null
        )
        whenever(usuarioRepository.findByFirebaseUid(firebaseUser.uid)).thenReturn(usuario)
        whenever(servicioPsicologo.crearPsicologo(eq(usuario), any())).thenReturn(
            PsicologoResponse(
                id = 10L,
                idEntidadPsicologo = 1L,
                firebaseUid = firebaseUser.uid,
                nombre = "Nombre",
                apellidos = "Apellidos",
                fotoPerfilUrl = null,
                numeroColegiado = "1234",
                especialidades = listOf("clinica"),
                descripcion = null
            )
        )

        mockMvc.perform(
            post("/api/psicologos/me")
                .with(withPacienteUser())
                .contentType("application/json")
                .content("""{"numeroColegiado":"1234","especialidades":["clinica"]}""")
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
                .content("""{"numeroColegiado":"1234","especialidades":["clinica"]}""")
        )
            .andExpect(status().isConflict)

        verify(usuarioRepository).findByFirebaseUid(firebaseUser.uid)
        verifyNoInteractions(servicioPsicologo)
    }

    @Test
    fun `PATCH api psicologos me especialidades devuelve 200 con lista actualizada`() {
        val respuesta = PsicologoResponse(
            id = 1L,
            idEntidadPsicologo = 1L,
            firebaseUid = firebaseUser.uid,
            nombre = "Nombre",
            apellidos = "Apellidos",
            fotoPerfilUrl = null,
            numeroColegiado = "1234",
            especialidades = listOf("Ansiedad", "Depresión"),
            descripcion = null
        )
        whenever(servicioPsicologo.actualizarEspecialidades(any(), any())).thenReturn(respuesta)

        mockMvc.perform(
            patch("/api/psicologos/me/especialidades")
                .with(withPacienteUser())
                .contentType("application/json")
                .content("""{"especialidades":["Ansiedad","Depresión"]}""")
        ).andExpect(status().isOk)

        verify(servicioPsicologo).actualizarEspecialidades(any(), eq(listOf("Ansiedad", "Depresión")))
    }

    @Test
    fun `PATCH api psicologos me especialidades devuelve 400 con lista vacía`() {
        mockMvc.perform(
            patch("/api/psicologos/me/especialidades")
                .with(withPacienteUser())
                .contentType("application/json")
                .content("""{"especialidades":[]}""")
        ).andExpect(status().isBadRequest)

        verifyNoInteractions(servicioPsicologo)
    }

    @Test
    fun `GET api psicologos me devuelve 200 cuando existe`() {
        val respuesta = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 10L, firebaseUid = psicologoUser.uid,
            nombre = "Nombre", apellidos = "Apellidos", fotoPerfilUrl = null,
            numeroColegiado = "1234", especialidades = listOf("clinica"), descripcion = null,
        )
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId(psicologoUser.uid)).thenReturn(respuesta)

        mockMvc.perform(get("/api/psicologos/me").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firebaseUid").value(psicologoUser.uid))
    }

    @Test
    fun `GET api psicologos me devuelve 404 cuando no existe`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId(psicologoUser.uid)).thenReturn(null)

        mockMvc.perform(get("/api/psicologos/me").with(withPsicologoUser()))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET api psicologos firebaseId devuelve 200`() {
        val respuesta = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 10L, firebaseUid = "uid-psi",
            nombre = "Psi", apellidos = "Apellidos", fotoPerfilUrl = null,
            numeroColegiado = "1234", especialidades = listOf("clinica"), descripcion = null,
        )
        whenever(
            servicioPsicologo.obtenerPsicologoPorFirebaseIdConAutorizacion(firebaseUser.uid, "uid-psi")
        ).thenReturn(respuesta)

        mockMvc.perform(get("/api/psicologos/firebaseId/uid-psi").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.firebaseUid").value("uid-psi"))
    }

    @Test
    fun `GET api psicologos id devuelve 200`() {
        val respuesta = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 10L, firebaseUid = "uid-psi",
            nombre = "Psi", apellidos = "Apellidos", fotoPerfilUrl = null,
            numeroColegiado = "1234", especialidades = listOf("clinica"), descripcion = null,
        )
        whenever(servicioPsicologo.obtenerPsicologoPorIdConAutorizacion(firebaseUser.uid, 10L))
            .thenReturn(respuesta)

        mockMvc.perform(get("/api/psicologos/id/10").with(withPacienteUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.idEntidadPsicologo").value(10))
    }

    @Test
    fun `GET api psicologos me pacientes devuelve 200 con lista`() {
        val paciente = PacienteResponse(
            id = 2L, firebaseUid = "uid-pac", nombre = "Pac", apellidos = "Apellidos",
            fotoPerfilUrl = null, psicologoId = 10L, idPaciente = 20L,
        )
        whenever(servicioPsicologo.obtenerPacientesPorFirebaseId(psicologoUser.uid))
            .thenReturn(listOf(paciente))

        mockMvc.perform(get("/api/psicologos/me/pacientes").with(withPsicologoUser()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].idPaciente").value(20))
    }

    @Test
    fun `GET api psicologos me pacientes devuelve 204 cuando vacio`() {
        whenever(servicioPsicologo.obtenerPacientesPorFirebaseId(psicologoUser.uid)).thenReturn(emptyList())

        mockMvc.perform(get("/api/psicologos/me/pacientes").with(withPsicologoUser()))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `PATCH api psicologos me descripcion devuelve 200`() {
        val respuesta = PsicologoResponse(
            id = 1L, idEntidadPsicologo = 1L, firebaseUid = firebaseUser.uid,
            nombre = "Nombre", apellidos = "Apellidos", fotoPerfilUrl = null,
            numeroColegiado = "1234", especialidades = listOf("clinica"), descripcion = "Nueva bio",
        )
        whenever(servicioPsicologo.actualizarDescripcion(psicologoUser.uid, "Nueva bio")).thenReturn(respuesta)

        mockMvc.perform(
            patch("/api/psicologos/me/descripcion")
                .with(withPsicologoUser())
                .contentType("application/json")
                .content("""{"descripcion":"Nueva bio"}""")
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.descripcion").value("Nueva bio"))
    }
}
