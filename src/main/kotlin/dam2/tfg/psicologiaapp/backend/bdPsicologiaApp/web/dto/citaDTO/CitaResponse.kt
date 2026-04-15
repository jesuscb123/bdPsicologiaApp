package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.EstadoCita
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import java.time.OffsetDateTime

data class CitaResponse(
    val id: Long,
    val inicio: OffsetDateTime,
    val fin: OffsetDateTime,
    val psicologo: PsicologoResponse,
    val paciente: PacienteResponse,
    val estadoPersistido: EstadoCita,
    val estadoCalculado: EstadoCitaCalculado
)

