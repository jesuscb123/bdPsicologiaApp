package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseTokenFilter(
    private val firebaseService: FirebaseService,
    private val servicioRoles: ServicioRoles
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(FirebaseTokenFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7).trim()

        try {
            val usuarioFirebase = firebaseService.getUserFromToken(token)

            if (usuarioFirebase != null) {
                val roles = try {
                    servicioRoles.obtenerRolesPorFirebaseUid(usuarioFirebase.uid)
                } catch (e: Exception) {
                    log.warn(
                        "Token válido pero falló la carga de roles para uid={}; se sigue sin autoridades (p. ej. 403 en @PreAuthorize).",
                        usuarioFirebase.uid,
                        e
                    )
                    emptyList()
                }
                val authorities = roles.map { SimpleGrantedAuthority(it) }

                val authentication = UsernamePasswordAuthenticationToken(
                    usuarioFirebase,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            log.error("Error inesperado en FirebaseTokenFilter al procesar el token", e)
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}