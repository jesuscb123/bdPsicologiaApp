package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val firebaseTokenFilter: FirebaseTokenFilter,
    private val filtroRateLimit: FiltroRateLimit,
    private val environment: Environment
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val perfilesActivos = environment.activeProfiles.toSet()
        val esPerfllDev = perfilesActivos.contains("dev")

        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
            .authorizeHttpRequests { auth ->
                if (esPerfllDev) {
                    auth.requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    ).permitAll()
                }

                auth.requestMatchers(HttpMethod.GET, "/api/archivos/perfiles/**").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/usuarios/existe-email").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/mantener-activo").permitAll()

                auth.anyRequest().authenticated()
            }
            .addFilterBefore(filtroRateLimit, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * CORS centralizado. En perfiles de desarrollo se permite localhost:4200 (Angular/Swagger).
     * En producción solo se aceptan los orígenes explícitamente listados.
     * Cambia ALLOWED_ORIGINS_PROD para reflejar el dominio real del frontend.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val perfilesActivos = environment.activeProfiles.toSet()
        val esPerfllDev = perfilesActivos.contains("dev")

        val origenes = if (esPerfllDev) {
            listOf("http://localhost:4200", "http://localhost:8080")
        } else {
            listOf("https://psicologiaapp.com")
        }

        val config = CorsConfiguration().apply {
            allowedOrigins = origenes
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
            allowCredentials = false
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
