package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

data class PacienteResponse(
    val id: Long?,
    val usuario: UsuarioResponse,
    val psicologo: PsicologoResponse
)
