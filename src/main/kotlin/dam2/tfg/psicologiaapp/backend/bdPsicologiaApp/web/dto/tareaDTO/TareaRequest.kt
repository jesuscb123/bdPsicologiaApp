package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO

import jakarta.validation.constraints.NotBlank

data class TareaCrearRequest(
    @field:NotBlank(message = "El título no puede estar vacío")
    val titulo: String,

    @field:NotBlank(message = "La descripción no puede estar vacía")
    val descripcion: String
)

data class TareaActualizarRequest(
    @field:NotBlank(message = "El título no puede estar vacío")
    val titulo: String,

    @field:NotBlank(message = "La descripción no puede estar vacía")
    val descripcion: String
)

data class TareaActualizarRealizadaRequest(
    val realizada: Boolean
)