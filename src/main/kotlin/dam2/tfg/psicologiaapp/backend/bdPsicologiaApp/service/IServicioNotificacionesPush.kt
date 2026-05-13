package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

/**
 * Envoltorio sobre Firebase Cloud Messaging para enviar notificaciones push a los tokens
 * registrados de un usuario. Centralizar aquí el SDK permite que el resto de servicios
 * (tareas, chat, futuras citas) sean independientes del proveedor.
 */
interface IServicioNotificacionesPush {

    /** Notifica al destinatario sobre un nuevo mensaje de chat. */
    fun notificarNuevoMensajeChat(
        firebaseUidDestinatario: String,
        chatId: String,
        nombreRemitente: String,
        vistaPreviaTexto: String,
        pacienteId: Long,
        psicologoId: Long,
    )

    /** Notifica al paciente que su psicólogo le ha asignado una tarea nueva. */
    fun notificarNuevaTarea(
        firebaseUidPaciente: String,
        nombrePsicologo: String,
        tituloTarea: String,
        descripcionTarea: String,
        tareaId: Long,
    )

    /**
     * Notifica al psicólogo de que las últimas notas de un paciente presentan posibles indicios
     * de riesgo. El payload NO incluye contenido de las notas: solo el id y el nombre del
     * paciente, suficiente para abrir su ficha desde la notificación.
     */
    fun notificarAlertaRiesgo(
        firebaseUidPsicologo: String,
        pacienteId: Long,
        nombrePaciente: String,
    )
}
