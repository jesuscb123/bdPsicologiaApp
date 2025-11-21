package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException

@Configuration
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

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .build()

            // 4. Inicializa Firebase con estas opciones
            return FirebaseApp.initializeApp(options)

        } catch (e: IOException) {
            throw IllegalStateException("Error al inicializar Firebase desde las credenciales.", e)
        }
    }
}
