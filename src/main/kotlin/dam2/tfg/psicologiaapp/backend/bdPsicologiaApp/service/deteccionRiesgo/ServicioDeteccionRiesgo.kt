package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.GroqProperties
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.RiesgoIaProperties
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNotificacionesPush
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatMessage
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqResponseFormat
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementación de la detección automática de indicios de riesgo en las notas del paciente.
 *
 * Características clave:
 *  - Asíncrono ([Async]): no bloquea la respuesta HTTP al paciente que acaba de crear la nota.
 *  - Anti-spam: dedupe en memoria por `pacienteId` durante [RiesgoIaProperties.ventanaDedupeHoras].
 *  - A prueba de fallos: cualquier excepción (Groq caído, JSON malformado, paciente sin psicólogo)
 *    se loggea como warn y se ignora silenciosamente. La nota del paciente queda intacta.
 *  - Privacidad: no loggea contenido de notas ni respuesta IA; solo `pacienteId`, `nivel`,
 *    `latenciaMs` y `httpStatus`.
 */
@Service
class ServicioDeteccionRiesgo(
    private val pacienteRepository: PacienteRepository,
    private val notaRepository: NotaRepository,
    private val propiedadesGroq: GroqProperties,
    private val propiedadesRiesgo: RiesgoIaProperties,
    private val servicioNotificacionesPush: IServicioNotificacionesPush,
    @Qualifier("groqRestClient") private val clienteGroq: RestClient,
    transactionManager: PlatformTransactionManager,
) : IServicioDeteccionRiesgo {

    private val log = LoggerFactory.getLogger(ServicioDeteccionRiesgo::class.java)

    /** Tx readOnly para asegurar que las lecturas (JOIN FETCH + notas) ocurren con sesión abierta. */
    private val transactionTemplate: TransactionTemplate = TransactionTemplate(transactionManager).apply {
        isReadOnly = true
    }

    /**
     * Jackson tolerante: ignora propiedades extra y nulls; algunas respuestas de Groq vienen
     * con campos adicionales o el `nivel` en minúsculas que normaliza [NivelRiesgo.deTextoSeguro].
     */
    private val jsonMapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    /**
     * Última instante en el que se envió push al psicólogo para cada paciente. Se usa para evitar
     * notificar repetidamente sobre las mismas notas si el paciente edita varias seguidas.
     *
     * Limitación conocida: este mapa es local al proceso. Si el backend escala horizontalmente,
     * cada instancia tendrá su propio dedupe. Para un TFG con una sola instancia en Render es
     * suficiente; si en el futuro hay réplicas, mover esto a Redis o a una tabla.
     */
    private val ultimaAlertaPorPaciente: ConcurrentHashMap<Long, Instant> = ConcurrentHashMap()

    @Async
    override fun evaluarRiesgoUltimasNotasAsync(pacienteId: Long) {
        if (!propiedadesRiesgo.habilitado) return
        if (propiedadesGroq.apiKey.isBlank()) {
            // El servicio de IA está apagado por config. No loggeamos como warn para no llenar
            // los logs con el mismo mensaje cada vez que un paciente crea una nota.
            return
        }
        try {
            val datos = cargarDatosParaEvaluacion(pacienteId)
            if (datos == null) {
                log.debug("Evaluación de riesgo omitida para paciente {} (sin datos suficientes)", pacienteId)
                return
            }

            val resultado = clasificarConGroq(pacienteId, datos.notasAnonimas)
            log.info(
                "Detección de riesgo (paciente {}): nivel={}, notasAnalizadas={}",
                pacienteId,
                resultado.name,
                datos.totalNotas,
            )

            if (resultado == NivelRiesgo.ALTO) {
                if (estaDeduplicado(pacienteId)) {
                    log.debug("Push de riesgo omitido para paciente {} (dentro de la ventana de dedupe)", pacienteId)
                } else {
                    servicioNotificacionesPush.notificarAlertaRiesgo(
                        firebaseUidPsicologo = datos.firebaseUidPsicologo,
                        pacienteId = pacienteId,
                        nombrePaciente = datos.nombrePaciente,
                    )
                    ultimaAlertaPorPaciente[pacienteId] = Instant.now()
                }
            }
        } catch (e: Exception) {
            log.warn(
                "Fallo en la detección automática de riesgo (paciente {}): {}",
                pacienteId,
                e.message,
            )
        }
    }

    private fun estaDeduplicado(pacienteId: Long): Boolean {
        val ultimaAlerta = ultimaAlertaPorPaciente[pacienteId] ?: return false
        val ventana = Duration.ofHours(propiedadesRiesgo.ventanaDedupeHoras)
        return Duration.between(ultimaAlerta, Instant.now()) < ventana
    }

    /**
     * Carga paciente + psicólogo + últimas N notas dentro de una sola tx de solo lectura.
     * Devuelve null si:
     *  - El paciente no existe.
     *  - El paciente no tiene psicólogo asignado (no hay a quién notificar).
     *  - El paciente no tiene notas que analizar.
     */
    private fun cargarDatosParaEvaluacion(pacienteId: Long): DatosEvaluacion? =
        transactionTemplate.execute {
            val paciente = pacienteRepository.findByIdConPsicologoYUsuarios(pacienteId) ?: return@execute null
            val psicologo = paciente.psicologo ?: return@execute null
            val psicologoId = psicologo.id ?: return@execute null

            val notas = notaRepository.obtenerUltimasNotasPacienteParaPsicologo(
                pacienteId = pacienteId,
                psicologoId = psicologoId,
                pageable = PageRequest.of(0, NUMERO_MAXIMO_NOTAS),
            )
            if (notas.isEmpty()) return@execute null

            val nombrePaciente = listOf(paciente.usuario.nombre, paciente.usuario.apellidos)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Paciente" }

            DatosEvaluacion(
                nombrePaciente = nombrePaciente,
                firebaseUidPsicologo = psicologo.usuario.firebaseUid,
                notasAnonimas = anonimizarNotas(notas),
                totalNotas = notas.size,
            )
        }

    /** Mismo criterio de anonimización que el servicio de resumen: solo `asunto` + `descripcion`. */
    private fun anonimizarNotas(notas: List<Nota>): String =
        notas.joinToString(separator = "\n---\n") { nota ->
            val asunto = nota.asunto.trim().take(LIMITE_CARACTERES_ASUNTO)
            val descripcion = nota.descripcion.trim().take(LIMITE_CARACTERES_DESCRIPCION)
            buildString {
                append("Asunto: ").append(asunto).append('\n')
                append("Descripción: ").append(descripcion)
            }
        }

    private fun clasificarConGroq(pacienteId: Long, notasAnonimas: String): NivelRiesgo {
        val solicitud = GroqChatRequest(
            model = propiedadesGroq.modelo,
            messages = listOf(
                GroqChatMessage(role = "system", content = PROMPT_SISTEMA),
                GroqChatMessage(role = "user", content = construirPromptUsuario(notasAnonimas)),
            ),
            maxTokens = propiedadesRiesgo.maxTokens,
            temperature = propiedadesRiesgo.temperatura,
            responseFormat = GroqResponseFormat(type = "json_object"),
        )

        val instanteInicio = System.currentTimeMillis()
        val respuesta: GroqChatResponse? = try {
            clienteGroq.post()
                .uri("/chat/completions")
                .body(solicitud)
                .retrieve()
                .body(GroqChatResponse::class.java)
        } catch (e: RestClientException) {
            log.warn(
                "Fallo al llamar a Groq para detección de riesgo (paciente {}, latenciaMs {}): {}",
                pacienteId,
                System.currentTimeMillis() - instanteInicio,
                e.javaClass.simpleName,
            )
            return NivelRiesgo.NINGUNO
        }

        val latenciaMs = System.currentTimeMillis() - instanteInicio
        val contenido = respuesta
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (contenido == null) {
            log.warn(
                "Groq devolvió respuesta vacía en detección de riesgo (paciente {}, latenciaMs {})",
                pacienteId,
                latenciaMs,
            )
            return NivelRiesgo.NINGUNO
        }

        val payload = try {
            jsonMapper.readValue(contenido, DeteccionRiesgoIaPayload::class.java)
        } catch (e: Exception) {
            log.warn(
                "Respuesta de Groq no parseable como JSON en detección de riesgo (paciente {}): {}",
                pacienteId,
                e.message,
            )
            return NivelRiesgo.NINGUNO
        }

        return NivelRiesgo.deTextoSeguro(payload.nivel)
    }

    private fun construirPromptUsuario(notasAnonimas: String): String = buildString {
        appendLine("Estas son las últimas notas anónimas del paciente, ordenadas de más reciente a más antigua, separadas por '---'.")
        appendLine("Analízalas en busca de indicios de riesgo y devuelve EXCLUSIVAMENTE un JSON válido con esta forma:")
        appendLine("""{"nivel": "NINGUNO" | "BAJO" | "MEDIO" | "ALTO", "justificacion": "<una frase breve en español>"}""")
        appendLine()
        appendLine("Notas:")
        append(notasAnonimas)
    }

    private data class DatosEvaluacion(
        val nombrePaciente: String,
        val firebaseUidPsicologo: String,
        val notasAnonimas: String,
        val totalNotas: Int,
    )

    companion object {
        private const val NUMERO_MAXIMO_NOTAS = 5
        private const val LIMITE_CARACTERES_ASUNTO = 200
        private const val LIMITE_CARACTERES_DESCRIPCION = 2_000

        private val PROMPT_SISTEMA = """
            Eres un asistente clínico que ayuda a profesionales de psicología en España a triage de notas clínicas.
            Recibes las últimas notas COMPLETAMENTE ANÓNIMAS de un único paciente (solo asunto y descripción) y debes
            estimar el nivel de RIESGO de ideación o conducta suicida / autolítica que sugieren.

            Niveles posibles:
              - NINGUNO: no hay señales clínicas de riesgo.
              - BAJO: malestar emocional general, tristeza, baja autoestima, ansiedad, sin alusión a hacerse daño.
              - MEDIO: desesperanza marcada, autolesiones no letales, aislamiento severo, pensamientos pasivos de muerte sin plan.
              - ALTO: ideación suicida activa o explícita, planificación, despedidas, conductas de cierre (regalar pertenencias,
                preparar el final), referencias directas a acabar con la propia vida o autolesiones potencialmente letales.

            Reglas estrictas:
              - Responde ÚNICAMENTE con un objeto JSON válido, sin texto adicional, sin markdown, sin explicaciones previas.
              - El JSON debe tener exactamente las claves `nivel` (string en mayúsculas) y `justificacion` (string breve en español).
              - Si dudas entre dos niveles, escoge SIEMPRE el más alto (precaución clínica).
              - No incluyas datos personales, fechas o nombres en la justificación; describe el patrón observado.
              - Si las notas no tienen contenido clínico evaluable, devuelve `nivel: NINGUNO` con justificación breve.
        """.trimIndent()
    }
}
