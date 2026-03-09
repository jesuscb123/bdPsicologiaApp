package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotBlank

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "rol",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PsicologoRequest::class, name = "PSICOLOGO"),
    JsonSubTypes.Type(value = PacienteRequest::class, name = "PACIENTE")
)

sealed class UsuarioRequest {
    abstract val nombreUsuario: String
    abstract val fotoPerfilUrl: String?
    abstract val rol: String
}

data class PsicologoRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    override val nombreUsuario: String,

    override val fotoPerfilUrl: String?,

    @field:NotBlank(message = "El rol es obligatorio")
    override val rol: String = "PSICOLOGO",

    @field:NotBlank(message = "El número de colegiado es obligatorio para psicólogos")
    val numeroColegiado: String,

    @field:NotBlank(message = "La especialidad es obligatoria")
    val especialidad: String
) : UsuarioRequest()

data class PacienteRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    override val nombreUsuario: String,

    override val fotoPerfilUrl: String?,

    @field:NotBlank(message = "El rol es obligatorio")
    override val rol: String = "PACIENTE",

    val psicologoId: Long?

) : UsuarioRequest()