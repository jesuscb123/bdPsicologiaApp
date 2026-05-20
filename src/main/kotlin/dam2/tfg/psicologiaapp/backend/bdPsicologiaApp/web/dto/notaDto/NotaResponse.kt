package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import java.time.LocalDateTime

data class NotaResponse(
    val id: Long?,
    val asunto: String,
    val descripcion: String,
    val ultimaModificacion: LocalDateTime,
    val paciente: PacienteResponse,
    val psicologo: PsicologoResponse
)
