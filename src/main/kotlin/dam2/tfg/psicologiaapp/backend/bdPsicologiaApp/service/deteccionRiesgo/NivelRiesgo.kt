package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo

/**
 * Niveles que la IA puede asignar al evaluar las últimas notas de un paciente.
 *
 *  - [NINGUNO]: sin señales relevantes.
 *  - [BAJO]: malestar general, tristeza puntual, baja autoestima.
 *  - [MEDIO]: desesperanza marcada, autolesiones no letales, aislamiento severo.
 *  - [ALTO]: ideación suicida explícita, planificación, despedidas, conductas de cierre.
 *
 * Solo se envía push al psicólogo cuando el nivel es [ALTO]. El resto se registra solo en logs.
 */
enum class NivelRiesgo {
    NINGUNO,
    BAJO,
    MEDIO,
    ALTO,
    ;

    companion object {
        /** Parseo defensivo del valor que devuelve Groq (case-insensitive, fallback a NINGUNO). */
        fun deTextoSeguro(valor: String?): NivelRiesgo {
            val limpio = valor?.trim()?.uppercase() ?: return NINGUNO
            return entries.firstOrNull { it.name == limpio } ?: NINGUNO
        }
    }
}
