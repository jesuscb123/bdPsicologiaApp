package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

import jakarta.validation.constraints.NotBlank

class PsicologoRequest {
    @field:NotBlank val numeroColegiado: String,

}