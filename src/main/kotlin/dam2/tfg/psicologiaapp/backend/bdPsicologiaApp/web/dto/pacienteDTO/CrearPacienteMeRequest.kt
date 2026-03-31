package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.pacienteDTO

import jakarta.validation.constraints.Positive

data class CrearPacienteMeRequest(
    @field:Positive(message = "El ID del psicólogo debe ser un número positivo")
    val psicologoId: Long? = null
)

