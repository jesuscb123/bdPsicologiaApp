package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ActualizarEmailRequest(
    @field:NotBlank(message = "El email no puede estar vacío")
    @field:Email(message = "El formato del email no es válido")
    @field:Size(max = 254, message = "El email no puede superar 254 caracteres")
    val nuevoEmail: String
)

