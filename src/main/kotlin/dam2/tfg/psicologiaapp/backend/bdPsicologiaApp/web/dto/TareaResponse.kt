package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

data class TareaResponse(
    val id: Long?,
    val tituloTarea: String,
    val descripcionTarea: String,
    val psicologo: PsicologoResponse,
    val paciente: PacienteResponse
)
