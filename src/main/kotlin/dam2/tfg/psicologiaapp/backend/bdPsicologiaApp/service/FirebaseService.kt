package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import org.springframework.stereotype.Service

@Service
class FirebaseService {
    fun getUserFromToken(idToken: String): FirebaseUserData? {
        try {
            val cleanToken = idToken.replace("Bearer ", "")
            val decodedToken = FirebaseAuth.getInstance().verifyIdToken(cleanToken)
            return FirebaseUserData(uid = decodedToken.uid, email = decodedToken.email)
        } catch (e: Exception) {
            // Loggear el error es una buena práctica
            e.printStackTrace()
            return null
        }
    }
}