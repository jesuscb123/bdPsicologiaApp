package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.auth.FirebaseAuth
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
class FirebaseService {

    private val log = LoggerFactory.getLogger(FirebaseService::class.java)
    private val executor = Executors.newCachedThreadPool()

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
                val future = CompletableFuture.supplyAsync({
                    val decoded = FirebaseAuth.getInstance().verifyIdToken(cleanToken)
                    FirebaseUserData(uid = decoded.uid, email = decoded.email)
                }, executor)

                return future.get(VERIFICACION_TOKEN_TIMEOUT_S, TimeUnit.SECONDS)

            } catch (e: TimeoutException) {
                log.warn(
                    "verifyIdToken superó el timeout de {}s (intento {} de {})",
                    VERIFICACION_TOKEN_TIMEOUT_S, indice + 1, VERIFICACION_TOKEN_INTENTOS,
                )
                ultimaExcepcion = e
            } catch (e: Exception) {
                ultimaExcepcion = e
                log.warn(
                    "verifyIdToken falló (intento {} de {}): {} — {}",
                    indice + 1, VERIFICACION_TOKEN_INTENTOS,
                    e.javaClass.simpleName, e.message,
                )
                if (indice < VERIFICACION_TOKEN_INTENTOS - 1) {
                    try {
                        Thread.sleep(VERIFICACION_TOKEN_ESPERA_MS)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return null
                    }
                }
            }
        }
        log.error(
            "Token de Firebase no verificado tras {} intentos. Último error: {}",
            VERIFICACION_TOKEN_INTENTOS, ultimaExcepcion?.message,
        )
        return null
    }

    companion object {
        private const val VERIFICACION_TOKEN_INTENTOS = 2
        private const val VERIFICACION_TOKEN_TIMEOUT_S = 8L
        private const val VERIFICACION_TOKEN_ESPERA_MS = 200L
    }
}