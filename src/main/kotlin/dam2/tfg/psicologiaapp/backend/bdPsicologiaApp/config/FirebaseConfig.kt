package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.io.IOException

@Configuration
class FirebaseConfig {

    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)

    @Bean
    fun firebaseApp(): FirebaseApp? {
        if (FirebaseApp.getApps().isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        val credentialsJson = System.getenv("FIREBASE_CREDENTIALS")
        if (credentialsJson.isNullOrBlank()) {
            log.warn(
                "FIREBASE_CREDENTIALS no está configurado — Firebase deshabilitado. " +
                "La autenticación con token real no funcionará."
            )
            return null
        }

        return try {
            val credentialsStream = ByteArrayInputStream(credentialsJson.toByteArray(Charsets.UTF_8))
            val credenciales = GoogleCredentials.fromStream(credentialsStream)
            val idProyecto = System.getenv("FIREBASE_PROJECT_ID")?.trim()?.takeIf { it.isNotEmpty() }
                ?: extraerProjectIdDelJson(credentialsJson)

            val constructorOpciones = FirebaseOptions.builder().setCredentials(credenciales)
            if (!idProyecto.isNullOrEmpty()) {
                constructorOpciones.setProjectId(idProyecto)
            }
            val urlBaseDatos = System.getenv("FIREBASE_DATABASE_URL")?.trim()
            if (!urlBaseDatos.isNullOrEmpty()) {
                constructorOpciones.setDatabaseUrl(urlBaseDatos)
            }

            FirebaseApp.initializeApp(constructorOpciones.build()).also {
                log.info("Firebase inicializado correctamente (proyecto: {})", idProyecto)
            }
        } catch (e: IOException) {
            log.error("Error al inicializar Firebase desde las credenciales: {}", e.message)
            null
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
