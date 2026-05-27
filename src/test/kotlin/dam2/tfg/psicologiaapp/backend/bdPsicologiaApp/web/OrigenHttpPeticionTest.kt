package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

internal class OrigenHttpPeticionTest {

    @Test
    fun `basePublica omite puerto estandar http 80`() {
        val request = MockHttpServletRequest()
        request.scheme = "http"
        request.serverName = "api.example.com"
        request.serverPort = 80

        assertEquals("http://api.example.com", OrigenHttpPeticion.basePublica(request))
    }

    @Test
    fun `basePublica omite puerto estandar https 443`() {
        val request = MockHttpServletRequest()
        request.scheme = "https"
        request.serverName = "api.example.com"
        request.serverPort = 443

        assertEquals("https://api.example.com", OrigenHttpPeticion.basePublica(request))
    }

    @Test
    fun `basePublica incluye puerto no estandar`() {
        val request = MockHttpServletRequest()
        request.scheme = "http"
        request.serverName = "localhost"
        request.serverPort = 8080

        assertEquals("http://localhost:8080", OrigenHttpPeticion.basePublica(request))
    }

    @Test
    fun `basePublica normaliza esquema a minusculas`() {
        val request = MockHttpServletRequest()
        request.scheme = "HTTPS"
        request.serverName = "api.example.com"
        request.serverPort = 443

        assertEquals("https://api.example.com", OrigenHttpPeticion.basePublica(request))
    }
}
