package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.util.Optional

internal class ServicioChatTest {

    private val pacienteRepository: PacienteRepository = mock()
    private val psicologoRepository: PsicologoRepository = mock()

    private val servicio = ServicioChat(pacienteRepository, psicologoRepository)

    private fun usuarioPsicologo() =
        Usuario(1L, "uid-psi", "psi@test.com", "Psico", "Logo", null)

    private fun usuarioPaciente() =
        Usuario(2L, "uid-pac", "pac@test.com", "Paci", "Ente", null)

    private fun psicologoConUid(uid: String = "uid-psi"): Psicologo {
        val usuario = Usuario(1L, uid, "psi@test.com", "Psico", "Logo", null)
        return Psicologo(10L, usuario, "COL-001", "Clínica", null)
    }

    private fun pacienteAsignadoA(psicologo: Psicologo): Paciente =
        Paciente(20L, usuarioPaciente(), psicologo)

    private fun pacienteSinPsicologo(): Paciente =
        Paciente(20L, usuarioPaciente(), null)

    // ── asegurarChatPaciente ──────────────────────────────────────────────────

    @Test
    fun `asegurarChatPaciente lanza IllegalStateException cuando el paciente no existe`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.asegurarChatPaciente("uid-pac")
        }

        verify(pacienteRepository).findByIdFirebaseUsuario("uid-pac")
    }

    @Test
    fun `asegurarChatPaciente lanza IllegalStateException cuando el paciente no tiene psicologo asignado`() {
        whenever(pacienteRepository.findByIdFirebaseUsuario("uid-pac"))
            .thenReturn(pacienteSinPsicologo())

        assertThrows<IllegalStateException> {
            servicio.asegurarChatPaciente("uid-pac")
        }
    }

    // ── asegurarChatPsicologo ─────────────────────────────────────────────────

    @Test
    fun `asegurarChatPsicologo lanza IllegalStateException cuando el psicologo no existe`() {
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(null)

        assertThrows<IllegalStateException> {
            servicio.asegurarChatPsicologo("uid-psi", 20L)
        }

        verify(psicologoRepository).findByIdFirebaseUsuario("uid-psi")
    }

    @Test
    fun `asegurarChatPsicologo lanza IllegalStateException cuando el paciente no existe`() {
        val psicologo = psicologoConUid()
        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(psicologo)
        whenever(pacienteRepository.findById(99L)).thenReturn(Optional.empty())

        assertThrows<IllegalStateException> {
            servicio.asegurarChatPsicologo("uid-psi", 99L)
        }
    }

    @Test
    fun `asegurarChatPsicologo lanza SecurityException cuando el paciente no esta asignado a este psicologo`() {
        val psicologoPropietario = psicologoConUid("uid-psi")
        val otroUsuarioPsi = Usuario(3L, "uid-psi-otro", "otro@test.com", "Otro", "Psi", null)
        val otroPsicologo = Psicologo(99L, otroUsuarioPsi, "COL-999", "Otra", null)
        val pacienteDeOtroPsicologo = Paciente(20L, usuarioPaciente(), otroPsicologo)

        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(psicologoPropietario)
        whenever(pacienteRepository.findById(20L)).thenReturn(Optional.of(pacienteDeOtroPsicologo))

        val ex = assertThrows<SecurityException> {
            servicio.asegurarChatPsicologo("uid-psi", 20L)
        }

        assertEquals("El paciente con id 20 no está asignado a este psicólogo", ex.message)
    }

    @Test
    fun `asegurarChatPsicologo lanza SecurityException cuando el paciente no tiene ningun psicologo asignado`() {
        val psicologo = psicologoConUid()
        val pacienteSinAsignacion = pacienteSinPsicologo()

        whenever(psicologoRepository.findByIdFirebaseUsuario("uid-psi")).thenReturn(psicologo)
        whenever(pacienteRepository.findById(20L)).thenReturn(Optional.of(pacienteSinAsignacion))

        assertThrows<SecurityException> {
            servicio.asegurarChatPsicologo("uid-psi", 20L)
        }
    }
}
