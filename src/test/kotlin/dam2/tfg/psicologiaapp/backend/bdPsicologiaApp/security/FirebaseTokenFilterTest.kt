package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.security.core.context.SecurityContextHolder

internal class FirebaseTokenFilterTest {

    private lateinit var firebaseService: FirebaseService
    private lateinit var servicioRoles: ServicioRoles
    private lateinit var filter: FirebaseTokenFilter

    @BeforeEach
    fun setUp() {
        firebaseService = mock()
        servicioRoles = mock()
        filter = FirebaseTokenFilter(firebaseService, servicioRoles)
        SecurityContextHolder.clearContext()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `sin Authorization continua sin autenticacion`() {
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()
        whenever(request.getHeader("Authorization")).thenReturn(null)

        filter.doFilter(request, response, chain)

        verify(chain).doFilter(request, response)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `Bearer valido establece autenticacion con roles`() {
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()
        val user = FirebaseUserData("uid-1", "a@b.com")

        whenever(request.getHeader("Authorization")).thenReturn("Bearer token-valido")
        whenever(firebaseService.getUserFromToken("token-valido")).thenReturn(user)
        whenever(servicioRoles.obtenerRolesPorFirebaseUid("uid-1"))
            .thenReturn(listOf(ServicioRoles.ROL_PACIENTE))

        filter.doFilter(request, response, chain)

        val auth = SecurityContextHolder.getContext().authentication
        assertNotNull(auth)
        assertEquals(user, auth!!.principal)
        assertTrue(auth.authorities.any { it.authority == ServicioRoles.ROL_PACIENTE })
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `Bearer invalido continua sin autenticacion`() {
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()

        whenever(request.getHeader("Authorization")).thenReturn("Bearer token-invalido")
        whenever(firebaseService.getUserFromToken("token-invalido")).thenReturn(null)

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `error al verificar token limpia contexto y continua`() {
        val request = mock<HttpServletRequest>()
        val response = mock<HttpServletResponse>()
        val chain = mock<FilterChain>()

        whenever(request.getHeader("Authorization")).thenReturn("Bearer boom")
        whenever(firebaseService.getUserFromToken("boom")).thenThrow(RuntimeException("fallo firebase"))

        filter.doFilter(request, response, chain)

        assertNull(SecurityContextHolder.getContext().authentication)
        verify(chain).doFilter(request, response)
    }
}
