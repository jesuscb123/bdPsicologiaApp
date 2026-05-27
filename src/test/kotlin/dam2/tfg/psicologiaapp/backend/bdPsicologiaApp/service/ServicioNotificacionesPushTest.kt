package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

internal class ServicioNotificacionesPushTest {

    private val servicioFcmToken: IServicioFcmToken = mock()

    private val servicio = ServicioNotificacionesPush(null, servicioFcmToken)

    @Test
    fun `notificarNuevoMensajeChat no consulta tokens cuando Firebase no esta inicializado`() {
        servicio.notificarNuevoMensajeChat(
            firebaseUidDestinatario = "uid-dest",
            chatId = "paciente_1_psicologo_2",
            nombreRemitente = "Remitente",
            vistaPreviaTexto = "Hola",
            pacienteId = 1L,
            psicologoId = 2L,
        )

        verify(servicioFcmToken, never()).obtenerTokensDe(any())
    }

    @Test
    fun `notificarNuevaTarea no consulta tokens cuando Firebase no esta inicializado`() {
        servicio.notificarNuevaTarea(
            firebaseUidPaciente = "uid-pac",
            nombrePsicologo = "Dr Test",
            tituloTarea = "Tarea",
            descripcionTarea = "Descripción",
            tareaId = 5L,
        )

        verify(servicioFcmToken, never()).obtenerTokensDe(any())
    }

    @Test
    fun `notificarAlertaRiesgo no consulta tokens cuando Firebase no esta inicializado`() {
        servicio.notificarAlertaRiesgo(
            firebaseUidPsicologo = "uid-psi",
            pacienteId = 10L,
            nombrePaciente = "Paciente Test",
        )

        verify(servicioFcmToken, never()).obtenerTokensDe(any())
    }
}
