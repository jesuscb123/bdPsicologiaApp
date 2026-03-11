package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import java.time.LocalDateTime

data class TareaResponse(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val horaEnvio: LocalDateTime,
    val realizada: Boolean,
    val psicologo: PsicologoResponse,
    val paciente: PacienteResponse
)
