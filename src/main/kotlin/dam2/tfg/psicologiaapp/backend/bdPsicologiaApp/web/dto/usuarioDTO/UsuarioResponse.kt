package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO

data class UsuarioResponse(
    val id: Long?,
    val firebaseUid: String,
    val email: String,
    val nombreUsuario: String
)