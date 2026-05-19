package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CrearPsicologoMeRequest(
    @field:NotBlank(message = "El número de colegiado es obligatorio")
    @field:Size(
        min = 4,
        max = 15,
        message = "El número de colegiado debe tener entre 4 y 15 caracteres"
    )
    @field:Pattern(
        regexp = "^[A-Za-z0-9\\-\\.\\s]+$",
        message = "El número de colegiado tiene un formato inválido"
    )
    val numeroColegiado: String,

    @field:NotEmpty(message = "Debes indicar al menos una especialidad")
    @field:Size(max = 10, message = "Máximo 10 especialidades")
    val especialidades: List<@NotBlank @Size(max = 80) String>
)
