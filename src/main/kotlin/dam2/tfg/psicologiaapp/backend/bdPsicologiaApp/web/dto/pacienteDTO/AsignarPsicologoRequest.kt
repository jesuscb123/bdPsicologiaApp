package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.pacienteDTO

import jakarta.validation.constraints.NotNull

data class AsignarPsicologoRequest(
    @field:NotNull(message = "El ID del psicólogo es obligatorio")
    val psicologoId: Long
)
