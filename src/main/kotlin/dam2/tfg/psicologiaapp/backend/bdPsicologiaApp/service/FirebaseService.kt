package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.auth.FirebaseAuth
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import org.springframework.stereotype.Service

@Service
class FirebaseService {
    fun getUserFromToken(idToken: String): FirebaseUserData? {
        val perfilesActivos = (System.getenv("SPRING_PROFILES_ACTIVE") ?: "")
        val cleanToken = idToken.replace("Bearer ", "").trim()

        // Atajo de desarrollo local: permite autenticar en Swagger sin Firebase real.
        // Formato esperado: Authorization: Bearer dev:<uid>:<email>
        if (perfilesActivos.split(",").map { it.trim() }.contains("dev")) {
            if (cleanToken.startsWith("dev:")) {
                val partes = cleanToken.split(":")
                val uid = partes.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
                val email = partes.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "$uid@dev.local"
                return FirebaseUserData(uid = uid, email = email)
            }
        }

        try {
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(cleanToken)
            return FirebaseUserData(uid = decodedToken.uid, email = decodedToken.email)
        } catch (e: Exception) {
            // Loggear el error es una buena práctica
            e.printStackTrace()
            return null
        }
    }
}