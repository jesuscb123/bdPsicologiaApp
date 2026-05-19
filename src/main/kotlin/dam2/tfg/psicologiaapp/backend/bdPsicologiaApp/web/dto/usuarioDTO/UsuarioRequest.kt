package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

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
    abstract val nombre: String
    abstract val apellidos: String
    abstract val fotoPerfilUrl: String?
    abstract val rol: String
}

data class PsicologoRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    override val nombre: String,

    @field:NotBlank(message = "Los apellidos no pueden estar vacíos")
    @field:Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    override val apellidos: String,

    override val fotoPerfilUrl: String?,

    @field:NotBlank(message = "El rol es obligatorio")
    override val rol: String = "PSICOLOGO",

    @field:NotBlank(message = "El número de colegiado es obligatorio para psicólogos")
    @field:Size(max = 15, message = "El número de colegiado no puede superar los 15 caracteres")
    val numeroColegiado: String,

    @field:NotEmpty(message = "Debes indicar al menos una especialidad")
    @field:Size(max = 10, message = "Máximo 10 especialidades")
    val especialidades: List<@NotBlank @Size(max = 80) String>,

    @field:Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    val descripcion: String?
) : UsuarioRequest()

data class PacienteRequest(
    @field:NotBlank(message = "El nombre no puede estar vacío")
    @field:Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
    override val nombre: String,

    @field:NotBlank(message = "Los apellidos no pueden estar vacíos")
    @field:Size(max = 100, message = "Los apellidos no pueden superar los 100 caracteres")
    override val apellidos: String,

    override val fotoPerfilUrl: String?,

    @field:NotBlank(message = "El rol es obligatorio")
    override val rol: String = "PACIENTE",

    val psicologoId: Long?

) : UsuarioRequest()