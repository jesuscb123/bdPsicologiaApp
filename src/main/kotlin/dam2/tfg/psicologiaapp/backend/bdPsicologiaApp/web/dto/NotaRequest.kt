package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

import jakarta.validation.constraints.NotBlank

data class NotaRequest(
    @field:NotBlank val asunto: String,
    @field:NotBlank val descripcion: String
)
