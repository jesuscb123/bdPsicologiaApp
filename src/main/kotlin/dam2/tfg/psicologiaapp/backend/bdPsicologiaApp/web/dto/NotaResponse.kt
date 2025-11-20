package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

data class NotaResponse(
    val id: Long?,
    val asunto: String,
    val descripcion: String,
    val paciente: PacienteResponse,
    val psicologo: PsicologoResponse
)