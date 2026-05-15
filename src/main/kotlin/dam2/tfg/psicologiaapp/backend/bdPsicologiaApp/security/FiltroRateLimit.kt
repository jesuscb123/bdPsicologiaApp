package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap

/**
 * Rate-limiting in-memory por IP para el endpoint público /api/usuarios/existe-email.
 *
 * Lógica de ventana deslizante:
 *  - Se almacena, por IP, una lista con los timestamps (ms) de las últimas peticiones.
 *  - En cada petición se descartan las entradas más antiguas que VENTANA_MS.
 *  - Si tras limpiar quedan MAX_PETICIONES o más, se devuelve 429 directamente.
 *
 * Nota: al ser in-memory, el conteo se pierde al reiniciar el proceso y no se comparte
 * entre instancias en horizontal. Es suficiente para proteger una instancia única y evita
 * añadir dependencias externas (Redis). Para multi-instancia considerar Bucket4j + Redis.
 */
@Component
class FiltroRateLimit : OncePerRequestFilter() {

    private val contadorPorIp = ConcurrentHashMap<String, ArrayDeque<Long>>()

    companion object {
        private const val VENTANA_MS = 60_000L
        private const val MAX_PETICIONES = 5
        private const val RUTA_PROTEGIDA = "/api/usuarios/existe-email"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!request.requestURI.equals(RUTA_PROTEGIDA, ignoreCase = true)) {
            filterChain.doFilter(request, response)
            return
        }

        val ip = obtenerIp(request)
        val ahora = System.currentTimeMillis()

        val timestamps = contadorPorIp.getOrPut(ip) { ArrayDeque() }

        synchronized(timestamps) {
            val limite = ahora - VENTANA_MS
            while (timestamps.isNotEmpty() && timestamps.first() < limite) {
                timestamps.removeFirst()
            }

            if (timestamps.size >= MAX_PETICIONES) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write("""{"error":"Demasiadas peticiones. Inténtalo en un minuto."}""")
                return
            }

            timestamps.addLast(ahora)
        }

        filterChain.doFilter(request, response)
    }

    private fun obtenerIp(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        return if (!forwarded.isNullOrBlank()) {
            forwarded.split(",").first().trim()
        } else {
            request.remoteAddr ?: "desconocida"
        }
    }
}
