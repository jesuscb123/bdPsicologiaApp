package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

internal class FirebaseServiceTest {

    @Test
    fun `getUserFromToken devuelve null cuando Firebase no esta inicializado`() {
        val servicio = FirebaseService(null)

        val resultado = servicio.getUserFromToken("token-opaco-cualquiera")

        assertNull(resultado)
    }

    @Test
    fun `getUserFromToken ignora atajo dev cuando el perfil activo no es dev`() {
        val servicio = FirebaseService(null)

        val resultado = servicio.getUserFromToken("dev:uid-test:test@dev.local")

        if (System.getenv("SPRING_PROFILES_ACTIVE") == "dev") {
            assertNotNull(resultado)
            assertEquals("uid-test", resultado!!.uid)
            assertEquals("test@dev.local", resultado.email)
        } else {
            assertNull(resultado)
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SPRING_PROFILES_ACTIVE", matches = "dev")
    fun `getUserFromToken parsea atajo dev con uid y email`() {
        val servicio = FirebaseService(null)

        val resultado = servicio.getUserFromToken("dev:uid-test:test@dev.local")

        assertNotNull(resultado)
        assertEquals("uid-test", resultado!!.uid)
        assertEquals("test@dev.local", resultado.email)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SPRING_PROFILES_ACTIVE", matches = "dev")
    fun `getUserFromToken genera email por defecto cuando falta en atajo dev`() {
        val servicio = FirebaseService(null)

        val resultado = servicio.getUserFromToken("dev:solo-uid")

        assertNotNull(resultado)
        assertEquals("solo-uid", resultado!!.uid)
        assertEquals("solo-uid@dev.local", resultado.email)
    }
}
