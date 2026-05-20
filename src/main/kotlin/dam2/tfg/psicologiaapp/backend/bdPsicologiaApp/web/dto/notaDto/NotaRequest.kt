package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NotaRequest(
    @field:NotBlank(message = "El asunto no puede estar vacío")
    @field:Size(max = 100, message = "El asunto no puede superar los 100 caracteres")
    val asunto: String,

    @field:NotBlank(message = "La descripción no puede estar vacía")
    @field:Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    val descripcion: String
)
