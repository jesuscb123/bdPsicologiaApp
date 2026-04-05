package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioPerfilResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioResponse


interface IServicioUsuario {
    fun obtenerUsuarioByFireBaseId(idFirebase: String) : UsuarioResponse?

    fun obtenerUsuarios(): List<UsuarioResponse>

    fun crearUsuario(fireBaseUid: String, email: String, request: UsuarioRequest): UsuarioResponse

    fun obtenerPerfilUsuario(firebaseUid: String): UsuarioPerfilResponse

    fun actualizarEmailUsuario(firebaseUid: String, nuevoEmail: String): UsuarioPerfilResponse

    fun actualizarFotoPerfilUsuario(firebaseUid: String, fotoPerfilUrl: String): UsuarioPerfilResponse

    fun subirFotoPerfilDesdeArchivo(
        firebaseUid: String,
        bytes: ByteArray,
        tipoContenido: String?,
        basePublicaOrigen: String? = null,
    ): UsuarioPerfilResponse

    fun eliminarUsuario(firebaseUid: String)

}