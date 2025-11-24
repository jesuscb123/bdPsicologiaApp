package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse

data class TareaResponse(
    val id: Long?,
    val tituloTarea: String,
    val descripcionTarea: String,
    val psicologo: PsicologoResponse,
    val paciente: PacienteResponse
)
