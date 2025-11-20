package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                val serviceAccount = ClassPathResource("app-psicologia-bb226-firebase-adminsdk-fbsvc-8b3bb8c8ed.json").inputStream

                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()

                FirebaseApp.initializeApp(options)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
