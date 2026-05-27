package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

internal class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `manejarValidacion devuelve 400 con campos`() {
        val bindingResult = mock<BindingResult>()
        val fieldError = FieldError("req", "email", "inválido")
        whenever(bindingResult.fieldErrors).thenReturn(listOf(fieldError))
        val methodParameter = mock<MethodParameter>()
        val ex = MethodArgumentNotValidException(methodParameter, bindingResult)

        val response = handler.manejarValidacion(ex)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Datos de entrada inválidos", response.body!!["error"])
        assertEquals("inválido", (response.body!!["campos"] as Map<*, *>)["email"])
    }

    @Test
    fun `manejarSeguridadLogica devuelve 403 sin detalles internos`() {
        val response = handler.manejarSeguridadLogica(SecurityException("detalle interno"))

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("Acceso denegado", response.body!!["error"])
        assertFalse(response.body!!.containsKey("traceId"))
    }

    @Test
    fun `manejarEstadoIlegal devuelve 404 con traceId`() {
        val response = handler.manejarEstadoIlegal(IllegalStateException("no encontrado"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Recurso no encontrado", response.body!!["error"])
        assertNotNull(response.body!!["traceId"])
    }

    @Test
    fun `manejarArgumentoIlegal devuelve 400 con traceId`() {
        val response = handler.manejarArgumentoIlegal(IllegalArgumentException("mal"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Petición incorrecta", response.body!!["error"])
        assertNotNull(response.body!!["traceId"])
    }

    @Test
    fun `manejarConflicto devuelve 409`() {
        val response = handler.manejarConflicto(ConflictoRecursoException("Ya existe"))

        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertEquals("Ya existe", response.body!!["error"])
    }

    @Test
    fun `manejarGenerico devuelve 500 sin filtrar detalle`() {
        val response = handler.manejarGenerico(RuntimeException("error interno"))

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("Error interno del servidor", response.body!!["error"])
        assertNotNull(response.body!!["traceId"])
        assertFalse(response.body!!.values.any { it.toString().contains("error interno") })
    }
}
