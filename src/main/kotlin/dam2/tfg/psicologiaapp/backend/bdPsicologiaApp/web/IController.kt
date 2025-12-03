package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Rol
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

interface IController {
    fun errorTokenExpirado(): ResponseEntity<Any> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autorización inválido o expirado.")

    }

    fun errorExiste(usuarioFirebase: FirebaseUserData, rol: Rol): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El ${rol} con ${usuarioFirebase.uid} ya existe.")
    }
}