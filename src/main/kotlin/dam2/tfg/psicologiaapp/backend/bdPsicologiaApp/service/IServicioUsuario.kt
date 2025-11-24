package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest


interface IServicioUsuario {
    fun obtenerUsuarioByFireBaseId(idFirebase: String) : Usuario?

    fun obtenerUsuarios(): List<Usuario>

    fun crearUsuario(fireBaseUid: String, email: String, request: UsuarioRequest): Usuario

}