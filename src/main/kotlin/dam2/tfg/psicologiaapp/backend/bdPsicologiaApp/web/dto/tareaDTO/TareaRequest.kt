package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO

import jakarta.validation.constraints.NotBlank

data class TareaRequest(
    @field:NotBlank val tituloTarea: String,
    @field:NotBlank val descripcionTarea: String
)