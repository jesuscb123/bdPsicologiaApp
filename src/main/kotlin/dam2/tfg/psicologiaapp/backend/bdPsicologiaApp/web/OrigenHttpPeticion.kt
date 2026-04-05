package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import jakarta.servlet.http.HttpServletRequest

/**
 * Construye el origen público (scheme + host + puerto si no estándar) a partir de la petición.
 * Con `server.forward-headers-strategy` (p. ej. framework) detrás de un proxy, refleja HTTPS y el host público.
 */
object OrigenHttpPeticion {

    fun basePublica(request: HttpServletRequest): String {
        val esquema = request.scheme.lowercase()
        val host = request.serverName
        val puerto = request.serverPort
        val omitirPuerto = (esquema == "http" && puerto == 80) ||
            (esquema == "https" && puerto == 443)
        return buildString {
            append(esquema).append("://").append(host)
            if (!omitirPuerto) append(':').append(puerto)
        }
    }
}
