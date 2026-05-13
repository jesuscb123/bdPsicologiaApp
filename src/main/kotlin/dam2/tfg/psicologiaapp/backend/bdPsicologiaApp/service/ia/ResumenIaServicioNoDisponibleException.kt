package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia

/**
 * Indica que el proveedor de IA (Groq) no está disponible o ha respondido con un error
 * que el controller debe traducir a HTTP 503.
 *
 * Extiende `IllegalStateException` para encajar con el contrato documentado en el plan
 * ("traducir errores de Groq a IllegalStateException con mensaje seguro"), pero permite
 * al controller distinguirlo de los demás errores de estado (p. ej. "Sin notas" → 404)
 * mediante un `catch` específico antes del genérico.
 *
 * El mensaje que se pasa SIEMPRE debe ser un mensaje seguro (no contiene cuerpo de notas,
 * ni payload de Groq, ni datos del paciente). El log detallado se queda en el servicio.
 */
class ResumenIaServicioNoDisponibleException(
    mensajeSeguro: String,
    causa: Throwable? = null,
) : IllegalStateException(mensajeSeguro, causa)
