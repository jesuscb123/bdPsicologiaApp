package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "rol",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PsicologoPerfilResponse::class, name = "PSICOLOGO"),
    JsonSubTypes.Type(value = PacientePerfilResponse::class, name = "PACIENTE"),
    JsonSubTypes.Type(value = UsuarioPerfilBasicoResponse::class, name = "SIN_ROL"),
)
sealed class UsuarioPerfilResponse {
    abstract val id: Long
    abstract val firebaseUid: String
    abstract val nombreUsuario: String
    abstract val email: String
    abstract val fotoPerfilUrl: String?
    abstract val rol: String
}

data class PsicologoPerfilResponse(
    override val id: Long,
    override val firebaseUid: String,
    override val nombreUsuario: String,
    override val email: String,
    override val fotoPerfilUrl: String?,
    override val rol: String = "PSICOLOGO",
    val numeroColegiado: String,
    val especialidad: String
) : UsuarioPerfilResponse()

data class PacientePerfilResponse(
    override val id: Long,
    override val firebaseUid: String,
    override val nombreUsuario: String,
    override val email: String,
    override val fotoPerfilUrl: String?,
    override val rol: String = "PACIENTE",
    val psicologoId: Long?
) : UsuarioPerfilResponse()

data class UsuarioPerfilBasicoResponse(
    override val id: Long,
    override val firebaseUid: String,
    override val nombreUsuario: String,
    override val email: String,
    override val fotoPerfilUrl: String?,
    override val rol: String = "SIN_ROL"
) : UsuarioPerfilResponse()

