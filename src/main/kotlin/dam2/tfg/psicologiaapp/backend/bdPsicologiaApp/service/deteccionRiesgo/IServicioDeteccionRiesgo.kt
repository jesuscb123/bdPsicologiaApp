package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo

/**
 * Evalúa las últimas notas de un paciente buscando indicios de riesgo (ideación suicida,
 * autolesiones severas, desesperanza extrema, planificación de cierre, despedidas...).
 *
 * El contrato es "fire-and-forget":
 *  - Se invoca tras el commit de la creación/edición de una nota.
 *  - La ejecución es asíncrona; nunca bloquea la respuesta al paciente.
 *  - Si detecta nivel ALTO, manda push al psicólogo asignado.
 *  - Cualquier error (Groq caído, JSON malformado, key vacía) se loggea y se ignora silenciosamente.
 *  - El paciente nunca se entera de esta evaluación.
 */
interface IServicioDeteccionRiesgo {

    /**
     * Lanza la evaluación de las últimas notas del paciente identificado por [pacienteId].
     * Si por cualquier motivo no se puede evaluar, no se lanza excepción: el método falla en silencio.
     */
    fun evaluarRiesgoUltimasNotasAsync(pacienteId: Long)
}
