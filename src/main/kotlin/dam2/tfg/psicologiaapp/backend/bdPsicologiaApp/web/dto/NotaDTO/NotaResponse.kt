package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse


data class NotaResponse(
    val id: Long?,
    val asunto: String,
    val descripcion: String,
    val paciente: PacienteResponse,
    val psicologo: PsicologoResponse
)