package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.GroqProperties
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatMessage
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.GroqChatResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.ResumenIaServicioNoDisponibleException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.iaDTO.ResumenIaResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.LocalDateTime

/**
 * Servicio que genera un resumen IA de las últimas notas de un paciente usando Groq.
 *
 * Privacidad:
 *  - Solo se envían a Groq `asunto` + `descripcion` de las últimas N notas.
 *  - Nunca se envían nombres, apellidos, email, IDs, ni fechas absolutas.
 *  - Los logs registran solo `pacienteId`, `notasEnviadas`, `latenciaMs` y `httpStatus`;
 *    NUNCA el contenido de las notas ni la respuesta de Groq.
 *
 * Errores:
 *  - Si `GROQ_API_KEY` está vacía → [ResumenIaServicioNoDisponibleException] → 503.
 *  - Si el paciente no pertenece al psicólogo → [SecurityException] → 403.
 *  - Si el paciente no tiene notas → [IllegalStateException]("Sin notas") → 404.
 *  - Si Groq falla (timeout, 4xx, 5xx) → [ResumenIaServicioNoDisponibleException] → 503.
 */
@Service
class ServicioResumenIa(
    private val notaRepository: NotaRepository,
    private val servicioPaciente: IServicioPaciente,
    private val servicioPsicologo: IServicioPsicologo,
    private val propiedadesGroq: GroqProperties,
    @Qualifier("groqRestClient") private val clienteGroq: RestClient,
) : IServicioResumenIa {

    private val log = LoggerFactory.getLogger(ServicioResumenIa::class.java)

    override fun generarResumenNotasPaciente(uidPsicologo: String, pacienteId: Long): ResumenIaResponse {
        if (propiedadesGroq.apiKey.isBlank()) {
            log.warn(
                "Petición de resumen IA rechazada (paciente {}): GROQ_API_KEY ausente en el entorno.",
                pacienteId,
            )
            throw ResumenIaServicioNoDisponibleException(
                "El servicio de resumen con IA no está disponible en este momento."
            )
        }

        // Validación de propiedad psicólogo↔paciente (idéntica a ServicioNota).
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(uidPsicologo)
            ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId)
            ?: throw IllegalStateException("El paciente no existe")

        if (paciente.psicologoId != psicologo.idEntidadPsicologo) {
            throw SecurityException("No tienes permiso para generar el resumen de las notas de este paciente.")
        }

        // La lectura corre dentro de la transacción `@Transactional(readOnly = true)` que
        // `SimpleJpaRepository` aplica por defecto a las repositorios Spring Data. Al
        // terminar esta línea la transacción se cierra ANTES de la llamada HTTP a Groq,
        // así no mantenemos una conexión a la BD abierta durante varios segundos.
        val notas = notaRepository.obtenerUltimasNotasPacienteParaPsicologo(
            pacienteId = paciente.idPaciente,
            psicologoId = psicologo.idEntidadPsicologo,
            pageable = PageRequest.of(0, NUMERO_MAXIMO_NOTAS),
        )

        if (notas.isEmpty()) {
            // El controller mapea este mensaje a 404. No se llama a Groq.
            throw IllegalStateException("Sin notas")
        }

        val notasAnonimas = anonimizarNotasParaPrompt(notas)
        val resumen = invocarGroq(pacienteId = pacienteId, notasAnonimas = notasAnonimas, totalNotas = notas.size)

        return ResumenIaResponse(
            resumen = resumen,
            numeroNotasAnalizadas = notas.size,
            generadoEn = LocalDateTime.now(),
            modelo = propiedadesGroq.modelo,
        )
    }

    /**
     * Construye el bloque de notas que se envía a Groq.
     *
     * Solo se incluyen `asunto` y `descripcion` separadas por `---`. Sin nombres,
     * apellidos, email, IDs, ni fechas. Cualquier texto se trimea y se trunca para
     * evitar prompts gigantes.
     */
    private fun anonimizarNotasParaPrompt(notas: List<Nota>): String =
        notas.joinToString(separator = "\n---\n") { nota ->
            val asunto = nota.asunto.trim().take(LIMITE_CARACTERES_ASUNTO)
            val descripcion = nota.descripcion.trim().take(LIMITE_CARACTERES_DESCRIPCION)
            buildString {
                append("Asunto: ").append(asunto).append('\n')
                append("Descripción: ").append(descripcion)
            }
        }

    private fun invocarGroq(
        pacienteId: Long,
        notasAnonimas: String,
        totalNotas: Int,
    ): String {
        val solicitud = GroqChatRequest(
            model = propiedadesGroq.modelo,
            messages = listOf(
                GroqChatMessage(role = "system", content = PROMPT_SISTEMA),
                GroqChatMessage(role = "user", content = construirPromptUsuario(notasAnonimas)),
            ),
            maxTokens = propiedadesGroq.maxTokens,
            temperature = propiedadesGroq.temperatura,
        )

        val instanteInicio = System.currentTimeMillis()
        val respuesta: GroqChatResponse? = try {
            clienteGroq.post()
                .uri("/chat/completions")
                .body(solicitud)
                .retrieve()
                .body(GroqChatResponse::class.java)
        } catch (e: HttpStatusCodeException) {
            log.error(
                "Fallo Groq (paciente {}, notasEnviadas {}, httpStatusGroq {}, latenciaMs {}): {}",
                pacienteId,
                totalNotas,
                e.statusCode.value(),
                System.currentTimeMillis() - instanteInicio,
                e.javaClass.simpleName,
            )
            throw ResumenIaServicioNoDisponibleException(
                "El proveedor de IA respondió con un error. Inténtalo de nuevo en unos minutos.",
                e,
            )
        } catch (e: ResourceAccessException) {
            log.error(
                "Fallo de red Groq (paciente {}, notasEnviadas {}, latenciaMs {}): {}",
                pacienteId,
                totalNotas,
                System.currentTimeMillis() - instanteInicio,
                e.javaClass.simpleName,
            )
            throw ResumenIaServicioNoDisponibleException(
                "No se puede contactar con el proveedor de IA. Inténtalo de nuevo en unos minutos.",
                e,
            )
        } catch (e: RestClientException) {
            log.error(
                "Error inesperado al llamar a Groq (paciente {}, notasEnviadas {}, latenciaMs {}): {}",
                pacienteId,
                totalNotas,
                System.currentTimeMillis() - instanteInicio,
                e.javaClass.simpleName,
            )
            throw ResumenIaServicioNoDisponibleException(
                "El proveedor de IA no está disponible en este momento.",
                e,
            )
        }

        val latenciaMs = System.currentTimeMillis() - instanteInicio

        val textoResumen = respuesta
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (textoResumen == null) {
            log.warn(
                "Groq devolvió un cuerpo vacío o sin texto (paciente {}, notasEnviadas {}, latenciaMs {})",
                pacienteId,
                totalNotas,
                latenciaMs,
            )
            throw ResumenIaServicioNoDisponibleException(
                "El proveedor de IA no devolvió ningún resumen. Inténtalo de nuevo."
            )
        }

        log.info(
            "Resumen IA generado correctamente (paciente {}, notasEnviadas {}, latenciaMs {}, modelo {})",
            pacienteId,
            totalNotas,
            latenciaMs,
            propiedadesGroq.modelo,
        )

        return textoResumen
    }

    private fun construirPromptUsuario(notasAnonimas: String): String = buildString {
        appendLine(
            "A continuación se incluyen las últimas notas anónimas de un paciente, ordenadas de más reciente a más antigua, separadas por '---'."
        )
        appendLine(
            "Resume su contenido en español neutro, en 5-8 frases, identificando temas recurrentes, posibles patrones emocionales o de comportamiento y puntos de seguimiento sugeridos para la próxima sesión."
        )
        appendLine(
            "No inventes información que no aparezca explícitamente en las notas. No incluyas nombres, direcciones, fechas absolutas ni cualquier otro dato que pudiera identificar al paciente."
        )
        appendLine()
        appendLine("Notas:")
        append(notasAnonimas)
    }

    companion object {
        private const val NUMERO_MAXIMO_NOTAS = 5

        // Tope defensivo para que un asunto/descripción muy largo no infle el prompt.
        private const val LIMITE_CARACTERES_ASUNTO = 200
        private const val LIMITE_CARACTERES_DESCRIPCION = 2_000

        private val PROMPT_SISTEMA = """
            Eres un asistente clínico que ayuda a profesionales de psicología en España.
            Analizas notas clínicas COMPLETAMENTE ANÓNIMAS de un único paciente —solo recibes el asunto y la descripción de cada nota— y produces un resumen breve para uso interno del psicólogo.
            Reglas estrictas:
            - Responde SIEMPRE en español, en tono profesional y neutro.
            - No inventes datos que no aparezcan en las notas.
            - No pidas información identificativa adicional.
            - No incluyas advertencias legales, descargos de responsabilidad ni instrucciones de uso.
            - Si las notas carecen de información clínica útil, indícalo brevemente y termina.
        """.trimIndent()
    }
}
