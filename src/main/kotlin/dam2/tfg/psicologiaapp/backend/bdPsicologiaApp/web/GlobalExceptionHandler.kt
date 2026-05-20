package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.UUID

/**
 * Manejador global de excepciones.
 *
 * Objetivos de seguridad:
 *  - Nunca exponer al cliente stack traces, nombres de tablas ni rutas internas.
 *  - Responder con un cuerpo uniforme { error, traceId } para todos los errores.
 *  - Conservar el detalle técnico sólo en los logs del servidor (traceId sirve de correlación).
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** Validación Bean Validation (@Valid): 400 Bad Request con los mensajes de validación. */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun manejarValidacion(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errores = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "inválido") }
        return ResponseEntity.badRequest().body(mapOf("error" to "Datos de entrada inválidos", "campos" to errores))
    }

    /** Acceso denegado por lógica de negocio: siempre 403 sin exponer detalles. */
    @ExceptionHandler(SecurityException::class)
    fun manejarSeguridadLogica(ex: SecurityException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(mapOf("error" to "Acceso denegado"))
    }

    /** Recurso no encontrado: 404 sin detalles internos. */
    @ExceptionHandler(IllegalStateException::class)
    fun manejarEstadoIlegal(ex: IllegalStateException): ResponseEntity<Map<String, String>> {
        val traceId = UUID.randomUUID().toString()
        log.warn("[traceId={}] IllegalStateException: {}", traceId, ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to "Recurso no encontrado", "traceId" to traceId))
    }

    /** Argumento inválido de negocio: 400. */
    @ExceptionHandler(IllegalArgumentException::class)
    fun manejarArgumentoIlegal(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        val traceId = UUID.randomUUID().toString()
        log.warn("[traceId={}] IllegalArgumentException: {}", traceId, ex.message)
        return ResponseEntity.badRequest()
            .body(mapOf("error" to "Petición incorrecta", "traceId" to traceId))
    }

    /** Conflicto de recurso (p. ej. recurso ya existente): 409 Conflict. */
    @ExceptionHandler(ConflictoRecursoException::class)
    fun manejarConflicto(ex: ConflictoRecursoException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to (ex.message ?: "Conflicto de recurso")))

    /** Captura genérica de último recurso: 500 sin información interna. */
    @ExceptionHandler(Exception::class)
    fun manejarGenerico(ex: Exception): ResponseEntity<Map<String, String>> {
        val traceId = UUID.randomUUID().toString()
        log.error("[traceId={}] Error no controlado: {}", traceId, ex.message, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "Error interno del servidor", "traceId" to traceId))
    }
}
