package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val firebaseTokenFilter: FirebaseTokenFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // No hace falta CSRF en APIs REST con Tokens
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // API sin estado
            .authorizeHttpRequests { auth ->
                // ¡IMPORTANTE! Si tienes rutas públicas, ponlas aquí:
                // auth.requestMatchers("/api/publico/**").permitAll()

                // Todo lo demás requiere estar autenticado con un token válido
                auth.anyRequest().authenticated()
            }
            // Colocamos nuestro filtro de Firebase ANTES del filtro por defecto de Spring
            .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}