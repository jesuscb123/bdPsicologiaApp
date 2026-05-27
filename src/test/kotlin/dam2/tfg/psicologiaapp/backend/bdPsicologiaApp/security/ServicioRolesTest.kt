package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ServicioRolesTest {

    private val pacienteRepository: PacienteRepository = mock()
    private val psicologoRepository: PsicologoRepository = mock()
    private val servicio = ServicioRoles(pacienteRepository, psicologoRepository)

    @Test
    fun `devuelve ROLE_PACIENTE cuando existe paciente`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid")).thenReturn(mock())
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid")).thenReturn(null)

        val roles = servicio.obtenerRolesPorFirebaseUid("uid")

        assertEquals(listOf(ServicioRoles.ROL_PACIENTE), roles)
    }

    @Test
    fun `devuelve ROLE_PSICOLOGO cuando existe psicologo`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid")).thenReturn(null)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid")).thenReturn(mock())

        val roles = servicio.obtenerRolesPorFirebaseUid("uid")

        assertEquals(listOf(ServicioRoles.ROL_PSICOLOGO), roles)
    }

    @Test
    fun `devuelve ambos roles cuando usuario es paciente y psicologo`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid")).thenReturn(mock())
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid")).thenReturn(mock())

        val roles = servicio.obtenerRolesPorFirebaseUid("uid")

        assertEquals(
            setOf(ServicioRoles.ROL_PACIENTE, ServicioRoles.ROL_PSICOLOGO),
            roles.toSet(),
        )
    }

    @Test
    fun `devuelve lista vacia cuando no hay roles`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid")).thenReturn(null)
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid")).thenReturn(null)

        val roles = servicio.obtenerRolesPorFirebaseUid("uid")

        assertTrue(roles.isEmpty())
    }
}
