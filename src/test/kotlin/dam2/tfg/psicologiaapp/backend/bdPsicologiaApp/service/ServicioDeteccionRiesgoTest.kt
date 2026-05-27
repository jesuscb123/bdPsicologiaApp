package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.github.tomakehurst.wiremock.WireMockServer
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.RiesgoIaProperties
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

internal class ServicioDeteccionRiesgoTest {

    private val pacienteRepository: PacienteRepository = mock()
    private val notaRepository: NotaRepository = mock()
    private val servicioNotificacionesPush: IServicioNotificacionesPush = mock()

    private lateinit var wireMockServer: WireMockServer
    private lateinit var servicio: dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo.ServicioDeteccionRiesgo

    @BeforeEach
    fun setUp() {
        wireMockServer = GroqWireMockSupport.crearServidor()
        wireMockServer.start()
        val propiedadesGroq = GroqWireMockSupport.propiedadesGroq(wireMockServer)
        servicio = dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo.ServicioDeteccionRiesgo(
            pacienteRepository = pacienteRepository,
            notaRepository = notaRepository,
            propiedadesGroq = propiedadesGroq,
            propiedadesRiesgo = RiesgoIaProperties(habilitado = true, ventanaDedupeHoras = 6),
            servicioNotificacionesPush = servicioNotificacionesPush,
            clienteGroq = GroqWireMockSupport.clienteGroq(propiedadesGroq),
            transactionManager = TestTransactionManager(),
        )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    private fun pacienteConNotas(): Paciente {
        val usuarioPac = Usuario(2L, "uid-pac", "pac@b.com", "Ana", "García", null)
        val usuarioPsi = Usuario(1L, "uid-psi", "psi@b.com", "Dr", "López", null)
        val psicologo = Psicologo(10L, usuarioPsi, "COL-1", mutableListOf("Clínica"), null)
        return Paciente(20L, usuarioPac, psicologo)
    }

    @Test
    fun `evaluarRiesgo no llama a Groq cuando el modulo esta deshabilitado`() {
        val propiedadesGroq = GroqWireMockSupport.propiedadesGroq(wireMockServer)
        val servicioDeshabilitado = dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo.ServicioDeteccionRiesgo(
            pacienteRepository,
            notaRepository,
            propiedadesGroq,
            RiesgoIaProperties(habilitado = false),
            servicioNotificacionesPush,
            GroqWireMockSupport.clienteGroq(propiedadesGroq),
            TestTransactionManager(),
        )

        servicioDeshabilitado.evaluarRiesgoUltimasNotasAsync(20L)

        verify(pacienteRepository, never()).findByIdConPsicologoYUsuarios(any())
        verifyNoInteractions(servicioNotificacionesPush)
    }

    @Test
    fun `evaluarRiesgo notifica al psicologo cuando Groq devuelve riesgo ALTO`() {
        val paciente = pacienteConNotas()
        val nota = Nota(1L, "Asunto", "Contenido sensible", paciente, paciente.psicologo!!)
        whenever(pacienteRepository.findByIdConPsicologoYUsuarios(20L)).thenReturn(paciente)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqChatCompletion(
            wireMockServer,
            """{"nivel":"ALTO","justificacion":"Ideación activa"}""",
        )

        servicio.evaluarRiesgoUltimasNotasAsync(20L)

        verify(servicioNotificacionesPush).notificarAlertaRiesgo(
            firebaseUidPsicologo = "uid-psi",
            pacienteId = 20L,
            nombrePaciente = "Ana García",
        )
    }

    @Test
    fun `evaluarRiesgo no notifica cuando Groq devuelve riesgo BAJO`() {
        val paciente = pacienteConNotas()
        val nota = Nota(1L, "Asunto", "Malestar leve", paciente, paciente.psicologo!!)
        whenever(pacienteRepository.findByIdConPsicologoYUsuarios(20L)).thenReturn(paciente)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqChatCompletion(
            wireMockServer,
            """{"nivel":"BAJO","justificacion":"Tristeza general"}""",
        )

        servicio.evaluarRiesgoUltimasNotasAsync(20L)

        verify(servicioNotificacionesPush, never()).notificarAlertaRiesgo(any(), any(), any())
    }

    @Test
    fun `evaluarRiesgo omite segunda alerta dentro de ventana dedupe`() {
        val paciente = pacienteConNotas()
        val nota = Nota(1L, "Asunto", "Contenido", paciente, paciente.psicologo!!)
        whenever(pacienteRepository.findByIdConPsicologoYUsuarios(20L)).thenReturn(paciente)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqChatCompletion(
            wireMockServer,
            """{"nivel":"ALTO","justificacion":"Riesgo"}""",
        )

        servicio.evaluarRiesgoUltimasNotasAsync(20L)
        servicio.evaluarRiesgoUltimasNotasAsync(20L)

        verify(servicioNotificacionesPush, times(1)).notificarAlertaRiesgo(any(), any(), any())
    }

    @Test
    fun `evaluarRiesgo no notifica cuando Groq falla con error HTTP`() {
        val paciente = pacienteConNotas()
        val nota = Nota(1L, "A", "D", paciente, paciente.psicologo!!)
        whenever(pacienteRepository.findByIdConPsicologoYUsuarios(20L)).thenReturn(paciente)
        whenever(
            notaRepository.obtenerUltimasNotasPacienteParaPsicologo(eq(20L), eq(10L), any()),
        ).thenReturn(listOf(nota))

        GroqWireMockSupport.stubGroqError(wireMockServer, 503)

        servicio.evaluarRiesgoUltimasNotasAsync(20L)

        verify(servicioNotificacionesPush, never()).notificarAlertaRiesgo(any(), any(), any())
    }
}
