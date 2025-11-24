package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse


data class PsicologoResponse(
    val id: Long?,
    val numeroColegiado: String,
    val especialidad: String,
    val usuario: UsuarioResponse
) {
}