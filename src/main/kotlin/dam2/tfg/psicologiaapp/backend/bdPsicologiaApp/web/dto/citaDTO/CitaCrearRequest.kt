package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class CitaCrearRequest(
    @field:NotNull(message = "La fecha de inicio es obligatoria")
    val inicio: OffsetDateTime,

    /** Zona horaria IANA, p. ej. "Europe/Madrid". */
    @field:NotBlank(message = "La zona horaria es obligatoria")
    @field:Size(max = 100, message = "La zona horaria no puede superar 100 caracteres")
    val zonaHoraria: String
)

