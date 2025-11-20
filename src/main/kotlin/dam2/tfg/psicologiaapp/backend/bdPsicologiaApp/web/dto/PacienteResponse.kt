package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario

data class PacienteResponse(
    val id: Long?,
    val usuario: Usuario,
    val psicologo: PsicologoResponse
)
