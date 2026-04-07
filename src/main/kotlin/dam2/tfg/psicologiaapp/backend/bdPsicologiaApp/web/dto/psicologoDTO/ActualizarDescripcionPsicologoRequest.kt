package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import jakarta.validation.constraints.Size

data class ActualizarDescripcionPsicologoRequest(
    @field:Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    val descripcion: String?
)

