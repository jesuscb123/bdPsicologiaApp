package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NotaRequest(
    @field:NotBlank
    @field:Size(max = 200, message = "El asunto no puede superar 200 caracteres")
    val asunto: String,

    @field:NotBlank
    @field:Size(max = 5000, message = "La descripción no puede superar 5000 caracteres")
    val descripcion: String
)
