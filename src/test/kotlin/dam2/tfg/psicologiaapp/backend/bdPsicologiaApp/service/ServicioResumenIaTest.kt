package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.github.tomakehurst.wiremock.WireMockServer
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.GroqProperties
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.ResumenIaServicioNoDisponibleException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.mockito.kotlin.*
import org.junit.jupiter.api.Assertions.assertEquals

@Execution(ExecutionMode.SAME_THREAD)
internal class ServicioResumenIaTest {

    private val notaRepository: NotaRepository = mock()
    private val servicioPaciente: IServicioPaciente = mock()
    private val servicioPsicologo: IServicioPsicologo = mock()

    private lateinit var wireMockServer: WireMockServer
    private lateinit var servicio: ServicioResumenIa

    @BeforeEach
    fun setUp() {
        wireMockServer = GroqWireMockSupport.crearServidor()
        wireMockServer.start()
        val propiedadesGroq = GroqWireMockSupport.propiedadesGroq(wireMockServer)
        servicio = ServicioResumenIa(
            notaRepository,
            servicioPaciente,
            servicioPsicologo,
            propiedadesGroq,
            GroqWireMockSupport.clienteGroq(propiedadesGroq),
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    private fun psicologoResponse() = PsicologoResponse(
        id = 1L,
        idEntidadPsicologo = 10L,
        firebaseUid = "uid-psi",
        nombre = "Psi",
        apellidos = "Logo",
        fotoPerfilUrl = null,
        numeroColegiado = "COL-1",
        especialidades = listOf("Clínica"),
        descripcion = null,
    )

    private fun pacienteResponse() = PacienteResponse(
        id = 2L,
        firebaseUid = "uid-pac",
        nombre = "Pac",
        apellidos = "Ente",
        fotoPerfilUrl = null,
        psicologoId = 10L,
        idPaciente = 20L,
    )

    @Test
    fun `generarResumen lanza 503 cuando falta api key`() {
        val servicioSinKey = ServicioResumenIa(
            notaRepository,
            servicioPaciente,
            servicioPsicologo,
            GroqProperties(apiKey = ""),
            mock(),
        )

        assertThrows<ResumenIaServicioNoDisponibleException> {
            servicioSinKey.generarResumenNotasPaciente("uid-psi", 20L)
        }
    }

    @Test
    fun `generarResumen lanza SecurityException cuando el paciente no pertenece al psicologo`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId("uid-psi")).thenReturn(psicologoResponse())
        whenever(servicioPaciente.obtenerPacienteId(20L)).thenReturn(
            pacienteResponse().copy(psicologoId = 99L),
        )

        assertThrows<SecurityException> {
            servicio.generarResumenNotasPaciente("uid-psi", 20L)
        }
    }

    @Test
    fun `generarResumen lanza cuando el paciente no tiene notas`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId("uid-psi")).thenReturn(psicologoResponse())
        whenever(servicioPaciente.obtenerPacienteId(20L)).thenReturn(pacienteResponse())
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(emptyList())

        val ex = assertThrows<IllegalStateException> {
            servicio.generarResumenNotasPaciente("uid-psi", 20L)
        }
        assertEquals("Sin notas", ex.message)
    }

    @Test
    fun `generarResumen devuelve resumen cuando Groq responde correctamente`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId("uid-psi")).thenReturn(psicologoResponse())
        whenever(servicioPaciente.obtenerPacienteId(20L)).thenReturn(pacienteResponse())
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Ente", null)
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Logo", null)
        val psicologo = Psicologo(10L, usuarioPsi, "COL-1", mutableListOf("Clínica"), null)
        val paciente = Paciente(20L, usuarioPac, psicologo)
        val nota = Nota(1L, "Sesión", "El paciente muestra mejoría", paciente, psicologo)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqChatCompletion(wireMockServer, "Resumen clínico breve del paciente.")

        val resultado = servicio.generarResumenNotasPaciente("uid-psi", 20L)

        assertEquals("Resumen clínico breve del paciente.", resultado.resumen)
        assertEquals(1, resultado.numeroNotasAnalizadas)
        assertEquals("llama-test", resultado.modelo)
    }

    @Test
    fun `generarResumen lanza 503 cuando Groq responde con error HTTP`() {
        whenever(servicioPsicologo.obtenerPsicologoFirebaseId("uid-psi")).thenReturn(psicologoResponse())
        whenever(servicioPaciente.obtenerPacienteId(20L)).thenReturn(pacienteResponse())
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Pac", "Ente", null)
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Psi", "Logo", null)
        val psicologo = Psicologo(10L, usuarioPsi, "COL-1", mutableListOf("Clínica"), null)
        val paciente = Paciente(20L, usuarioPac, psicologo)
        val nota = Nota(1L, "A", "D", paciente, psicologo)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqError(wireMockServer, 500)

        assertThrows<ResumenIaServicioNoDisponibleException> {
            servicio.generarResumenNotasPaciente("uid-psi", 20L)
        }
    }
}
