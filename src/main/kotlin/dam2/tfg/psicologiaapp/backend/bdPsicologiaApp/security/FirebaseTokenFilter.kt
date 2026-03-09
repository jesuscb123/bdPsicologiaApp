package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService // Ajusta el import a donde esté
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseTokenFilter(
    private val firebaseService: FirebaseService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        // Si no hay token, dejamos que siga (Spring Security bloqueará la ruta después)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7).trim()

        try {
            // Llamamos a tu servicio existente
            val usuarioFirebase = firebaseService.getUserFromToken(token)

            if (usuarioFirebase != null) {
                // ¡LA MAGIA! Creamos el contexto de seguridad inyectando TU objeto FirebaseUserData
                val authentication = UsernamePasswordAuthenticationToken(
                    usuarioFirebase, // Principal (El usuario)
                    null,            // Credentials (No usamos contraseñas aquí)
                    emptyList()      // Authorities (Roles, por ahora vacío)
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // Si el token expiró o es inválido, limpiamos por seguridad
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}