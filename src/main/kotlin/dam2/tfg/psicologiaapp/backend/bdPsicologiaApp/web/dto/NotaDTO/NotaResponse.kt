package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse

data class NotaResponse(
    val id: Long?,
    val asunto: String,
    val descripcion: String,
    val paciente: PacienteResponse,
    val psicologo: PsicologoResponse
)