package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse

data class TareaResponse(
    val id: Long?,
    val tituloTarea: String,
    val descripcionTarea: String,
    val psicologo: PsicologoResponse,
    val paciente: PacienteResponse
)
