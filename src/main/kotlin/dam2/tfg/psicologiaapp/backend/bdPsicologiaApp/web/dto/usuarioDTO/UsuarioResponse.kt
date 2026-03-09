package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "rol"
)
sealed class UsuarioResponse {
    abstract val id: Long
    abstract val firebaseUid: String
    abstract val nombreUsuario: String
    abstract val fotoPerfilUrl: String?
    abstract val rol: String
}

data class PsicologoResponse(
    override val id: Long,
    override val firebaseUid: String,
    override val nombreUsuario: String,
    override val fotoPerfilUrl: String?,
    override val rol: String = "PSICOLOGO",
    val numeroColegiado: String,
    val especialidad: String
) : UsuarioResponse()

data class PacienteResponse(
    override val id: Long,
    override val firebaseUid: String,
    override val nombreUsuario: String,
    override val fotoPerfilUrl: String?,
    override val rol: String = "PACIENTE"

) : UsuarioResponse()