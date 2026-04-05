package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException

@Configuration
@Profile("!dev")
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        // Evita inicializar la app múltiples veces si ya existe
        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        // 1. Lee la variable de entorno que creaste en Render
        val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")

        // 2. Comprueba si la variable existe. Si no, es un error fatal.
        if (credentialsJson.isNullOrBlank()) {
            throw IllegalStateException(
                "La variable de entorno FIREBASE_CREDENTIALS no está definida. " +
                        "Asegúrate de haberla configurado en el panel de Render."
            )
        }

        try {
            // 3. Convierte el String JSON de la variable en un "stream" que Firebase puede leer
            val credentialsStream = ByteArrayInputStream(credentialsJson.toByteArray(Charsets.UTF_8))
            val credenciales = GoogleCredentials.fromStream(credentialsStream)
            val idProyecto = System.getenv("FIREBASE_PROJECT_ID")?.trim()?.takeIf { it.isNotEmpty() }
                ?: extraerProjectIdDelJson(credentialsJson)

            val constructorOpciones = FirebaseOptions.builder()
                .setCredentials(credenciales)
            if (!idProyecto.isNullOrEmpty()) {
                constructorOpciones.setProjectId(idProyecto)
            }
            val options = constructorOpciones.build()

            // 4. Inicializa Firebase con estas opciones
            return FirebaseApp.initializeApp(options)

        } catch (e: IOException) {
            throw IllegalStateException("Error al inicializar Firebase desde las credenciales.", e)
        }
    }

    private fun extraerProjectIdDelJson(json: String): String? = runCatching {
        val clave = "\"project_id\""
        val idx = json.indexOf(clave)
        if (idx < 0) return@runCatching null
        val inicio = json.indexOf('"', idx + clave.length).takeIf { it >= 0 } ?: return@runCatching null
        val fin = json.indexOf('"', inicio + 1).takeIf { it > inicio } ?: return@runCatching null
        json.substring(inicio + 1, fin)
    }.getOrNull()
}
