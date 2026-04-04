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

        var ultimaExcepcion: Exception? = null
        repeat(VERIFICACION_TOKEN_INTENTOS) { indice ->
            try {
                val decodedToken = FirebaseAuth.getInstance().verifyIdToken(cleanToken)
                return FirebaseUserData(uid = decodedToken.uid, email = decodedToken.email)
            } catch (e: Exception) {
                ultimaExcepcion = e
                if (indice < VERIFICACION_TOKEN_INTENTOS - 1) {
                    try {
                        Thread.sleep(VERIFICACION_TOKEN_ESPERA_MS)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        ultimaExcepcion?.printStackTrace()
                        return null
                    }
                }
            }
        }
        ultimaExcepcion?.printStackTrace()
        return null
    }

    companion object {
        private const val VERIFICACION_TOKEN_INTENTOS = 3
        private const val VERIFICACION_TOKEN_ESPERA_MS = 100L
    }
}