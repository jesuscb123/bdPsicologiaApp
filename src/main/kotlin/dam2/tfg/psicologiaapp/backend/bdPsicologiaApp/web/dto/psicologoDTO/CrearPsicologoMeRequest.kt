package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CrearPsicologoMeRequest(
    @field:NotBlank(message = "El número de colegiado es obligatorio")
    @field:Size(
        min = 4,
        max = 32,
        message = "El número de colegiado debe tener entre 4 y 32 caracteres"
    )
    @field:Pattern(
        regexp = "^[A-Za-z0-9\\-\\.\\s]+$",
        message = "El número de colegiado tiene un formato inválido"
    )
    val numeroColegiado: String,

    @field:NotBlank(message = "La especialidad es obligatoria")
    @field:Size(
        min = 2,
        max = 80,
        message = "La especialidad debe tener entre 2 y 80 caracteres"
    )
    val especialidad: String
)

