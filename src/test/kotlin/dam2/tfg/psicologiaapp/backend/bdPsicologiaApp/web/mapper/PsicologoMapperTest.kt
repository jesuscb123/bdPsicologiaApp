package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PsicologoMapperTest {

    private val usuario = Usuario(1L, "uid-psi", "psi@test.com", "Carlos", "Ruiz", null)

    @Test
    fun `toEntity mapea campos del request`() {
        val request = PsicologoRequest(
            nombre = "Carlos",
            apellidos = "Ruiz",
            fotoPerfilUrl = null,
            rol = "PSICOLOGO",
            numeroColegiado = "12345",
            especialidades = listOf("Ansiedad", "Depresión"),
            descripcion = "Desc",
        )

        val entity = PsicologoMapper.toEntity(request, usuario)

        assertSame(usuario, entity.usuario)
        assertEquals("12345", entity.numeroColegiado)
        assertEquals(listOf("Ansiedad", "Depresión"), entity.especialidades)
        assertEquals("Desc", entity.descripcion)
    }

    @Test
    fun `toResponse mapea psicologo completo`() {
        val psicologo = Psicologo(10L, usuario, "12345", mutableListOf("Clinica"), "Bio")

        val response = PsicologoMapper.toResponse(psicologo)

        assertEquals(1L, response.id)
        assertEquals(10L, response.idEntidadPsicologo)
        assertEquals("uid-psi", response.firebaseUid)
        assertEquals("12345", response.numeroColegiado)
        assertEquals(listOf("Clinica"), response.especialidades)
        assertEquals("Bio", response.descripcion)
    }

    @Test
    fun `toResponse lanza cuando falta id entidad psicologo`() {
        val psicologo = Psicologo(null, usuario, "12345", mutableListOf("Clinica"), null)

        assertThrows<IllegalStateException> {
            PsicologoMapper.toResponse(psicologo)
        }
    }
}
