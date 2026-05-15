package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
class FirebaseService(@Autowired(required = false) private val firebaseApp: FirebaseApp?) {

    private val log = LoggerFactory.getLogger(FirebaseService::class.java)
    private val executor = Executors.newCachedThreadPool()

    fun getUserFromToken(idToken: String): FirebaseUserData? {
        val cleanToken = idToken.replace("Bearer ", "").trim()

        // Atajo de desarrollo local: permite autenticar en Swagger sin Firebase real.
        // Formato esperado: Authorization: Bearer dev:<uid>:<email>
        //
        // Endurecimiento de seguridad: el atajo SOLO se activa cuando el ÚNICO perfil
        // activo es exactamente "dev" (case-insensitive). Si SPRING_PROFILES_ACTIVE
        // está vacío o contiene cualquier otro perfil (p. ej. "prod,dev"), el atajo
        // permanece desactivado para evitar suplantación accidental en producción.
        if (atajoDevActivado() && cleanToken.startsWith("dev:")) {
            val partes = cleanToken.split(":")
            val uid = partes.getOrNull(1)?.takeIf { it.isNotBlank() } ?: return null
            val email = partes.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "$uid@dev.local"
            log.warn(
                "Atajo de autenticación 'dev:' usado para uid='{}'. Modo INSEGURO reservado a desarrollo local.",
                uid,
            )
            return FirebaseUserData(uid = uid, email = email)
        }

        if (firebaseApp == null) {
            log.error("FirebaseApp no está inicializado (¿falta FIREBASE_CREDENTIALS en las variables de entorno?)")
            return null
        }

        var ultimaExcepcion: Exception? = null
        repeat(VERIFICACION_TOKEN_INTENTOS) { indice ->
            try {
                val auth = FirebaseAuth.getInstance(firebaseApp)
                val future = CompletableFuture.supplyAsync({
                    val decoded = auth.verifyIdToken(cleanToken)
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

    /**
     * Indica si el atajo de autenticación `dev:` debe estar habilitado.
     *
     * Reglas (defensivas):
     * - Si `SPRING_PROFILES_ACTIVE` es nulo o queda vacío tras normalizar, devuelve `false`.
     * - Si hay más de un perfil activo, devuelve `false` (no se admite combinación con `dev`).
     * - Sólo devuelve `true` cuando el único perfil activo, ignorando mayúsculas y espacios,
     *   es exactamente `dev`.
     */
    private fun atajoDevActivado(): Boolean {
        val perfilesActivos = System.getenv("SPRING_PROFILES_ACTIVE")
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotEmpty() }
            ?: return false

        return perfilesActivos.size == 1 && perfilesActivos.single() == PERFIL_DEV
    }

    companion object {
        private const val VERIFICACION_TOKEN_INTENTOS = 2
        private const val VERIFICACION_TOKEN_TIMEOUT_S = 8L
        private const val VERIFICACION_TOKEN_ESPERA_MS = 200L
        private const val PERFIL_DEV = "dev"
    }
}