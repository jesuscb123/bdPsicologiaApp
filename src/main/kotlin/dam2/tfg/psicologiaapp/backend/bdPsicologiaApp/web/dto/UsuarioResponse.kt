package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

data class UsuarioResponse(
    val id: Long,
    val email: String,
    val nombreUsuario: String,
    val esPsicologo: Boolean,
    val esPaciente: Boolean
)