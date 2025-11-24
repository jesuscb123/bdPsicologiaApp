package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse

data class PacienteResponse(
    val id: Long?,
    val usuario: UsuarioResponse,
    val psicologo: PsicologoResponse
)
