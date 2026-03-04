package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import jakarta.validation.constraints.NotBlank

data class UsuarioRequest(
    val nombreUsuario: String,
    val fotoPerfilUrl: String?,
    val numeroColegiado: String?,
    val especialidad: String?,
    val rol: String
)