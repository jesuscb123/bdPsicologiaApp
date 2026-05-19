package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ActualizarEspecialidadesPsicologoRequest(
    @field:NotEmpty(message = "La lista de especialidades no puede estar vacía")
    @field:Size(max = 10, message = "No se pueden indicar más de 10 especialidades")
    val especialidades: List<String>
)
