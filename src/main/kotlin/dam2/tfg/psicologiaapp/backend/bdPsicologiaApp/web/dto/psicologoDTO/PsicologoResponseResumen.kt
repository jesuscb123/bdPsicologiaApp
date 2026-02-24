package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

data class PsicologoResponseResumen(
    val id: Long?,
    val especialidad: String,
    val nombreUsuario: String,
    val fotoPerfil: String?
)