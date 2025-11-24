package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import jakarta.validation.constraints.NotBlank

data class PsicologoRequest(
    @field:NotBlank val numeroColegiado: String,
    val especialidad: String
) {


}