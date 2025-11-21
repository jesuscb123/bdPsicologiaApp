package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

import jakarta.validation.constraints.NotBlank

data class UsuarioRequest(
    @field:NotBlank val nombreUsuario: String
)