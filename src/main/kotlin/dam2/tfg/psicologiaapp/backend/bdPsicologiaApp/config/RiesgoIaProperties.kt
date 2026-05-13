package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuración del módulo de detección de indicios de riesgo en las notas del paciente.
 *
 *  - [habilitado]: si está a `false`, ninguna nota se evalúa y no se llama a Groq. Útil para
 *    desactivar el feature rápidamente (env var) sin redeploy.
 *  - [ventanaDedupeHoras]: tras alertar al psicólogo sobre un paciente, no se vuelve a alertar
 *    en esta ventana (dedupe en memoria) aunque las siguientes notas también disparen señal.
 *  - [maxTokens] y [temperatura]: parámetros de la llamada de clasificación. Mantener
 *    `temperatura` baja para que el modelo sea consistente al clasificar.
 */
@ConfigurationProperties(prefix = "ia.riesgo")
data class RiesgoIaProperties(
    val habilitado: Boolean = true,
    val ventanaDedupeHoras: Long = 6,
    val maxTokens: Int = 300,
    val temperatura: Double = 0.1,
)
