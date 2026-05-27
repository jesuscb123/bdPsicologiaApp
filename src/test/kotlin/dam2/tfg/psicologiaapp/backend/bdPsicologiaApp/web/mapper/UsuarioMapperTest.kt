package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioBasicoResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class UsuarioMapperTest {

    @Test
    fun `toEntity mapea campos del request y firebase`() {
        val request = PacienteRequest(
            nombre = "Ana",
            apellidos = "López",
            fotoPerfilUrl = "https://foto.jpg",
            rol = "PACIENTE",
            psicologoId = 1L,
        )

        val entity = UsuarioMapper.toEntity(request, "uid-1", "ana@test.com")

        assertEquals("uid-1", entity.firebaseUid)
        assertEquals("ana@test.com", entity.email)
        assertEquals("Ana", entity.nombre)
        assertEquals("López", entity.apellidos)
        assertEquals("https://foto.jpg", entity.fotoPerfilUrl)
        assertNull(entity.id)
    }

    @Test
    fun `toResponse mapea usuario con id`() {
        val usuario = Usuario(5L, "uid-5", "x@y.com", "Nombre", "Apellidos", null)

        val response = UsuarioMapper.toResponse(usuario)

        assertTrue(response is UsuarioBasicoResponse)
        assertEquals(5L, response.id)
        assertEquals("uid-5", response.firebaseUid)
        assertEquals("Nombre", response.nombre)
        assertEquals("Apellidos", response.apellidos)
    }

    @Test
    fun `toResponse lanza cuando id es nulo`() {
        val usuario = Usuario(null, "uid", "x@y.com", "N", "A", null)

        assertThrows<IllegalStateException> {
            UsuarioMapper.toResponse(usuario)
        }
    }

    @Test
    fun `merge actualiza nombre y apellidos`() {
        val entity = Usuario(1L, "uid", "x@y.com", "Viejo", "Apellido viejo", null)
        val request = PacienteRequest("Nuevo", "Apellido nuevo", null, "PACIENTE", null)

        val merged = UsuarioMapper.merge(entity, request)

        assertSame(entity, merged)
        assertEquals("Nuevo", entity.nombre)
        assertEquals("Apellido nuevo", entity.apellidos)
    }
}
