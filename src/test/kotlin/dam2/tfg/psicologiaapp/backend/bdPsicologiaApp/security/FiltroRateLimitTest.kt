package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

internal class FiltroRateLimitTest {

    private val filter = FiltroRateLimit()
    private val chain = mock<FilterChain>()

    @Test
    fun `ruta no protegida pasa sin limitar`() {
        val request = MockHttpServletRequest("GET", "/api/usuarios/me")
        request.remoteAddr = "10.0.0.1"
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, chain)

        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        verify(chain).doFilter(request, response)
    }

    @Test
    fun `sexta peticion a existe-email devuelve 429`() {
        val ip = "192.168.1.50"

        repeat(5) {
            val request = MockHttpServletRequest("GET", "/api/usuarios/existe-email")
            request.remoteAddr = ip
            val response = MockHttpServletResponse()
            filter.doFilter(request, response, chain)
            assertNotEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.status)
        }

        val request6 = MockHttpServletRequest("GET", "/api/usuarios/existe-email")
        request6.remoteAddr = ip
        val response6 = MockHttpServletResponse()
        filter.doFilter(request6, response6, chain)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response6.status)
        assertTrue(response6.contentAsString.contains("Demasiadas peticiones"))
    }

    @Test
    fun `usa primera IP de X-Forwarded-For`() {
        val forwardedIp = "203.0.113.10"

        repeat(5) {
            val request = MockHttpServletRequest("GET", "/api/usuarios/existe-email")
            request.addHeader("X-Forwarded-For", "$forwardedIp, 10.0.0.1")
            filter.doFilter(request, MockHttpServletResponse(), chain)
        }

        val request6 = MockHttpServletRequest("GET", "/api/usuarios/existe-email")
        request6.addHeader("X-Forwarded-For", forwardedIp)
        val response6 = MockHttpServletResponse()
        filter.doFilter(request6, response6, chain)

        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response6.status)
    }
}
