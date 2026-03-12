package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse

interface IServicioPsicologo {
    fun obtenerPsicologos(): List<PsicologoResponse>

    fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): PsicologoResponse?

    fun crearPsicologo(usuario: Usuario, psicologoRequest: PsicologoRequest): PsicologoResponse

    fun obtenerPsicologoId(id: Long): PsicologoResponse?

    fun obtenerEntidadPsicologo(id: Long): Psicologo

    fun buscarPsicologosPorNombre(nombreUsuario: String): List<PsicologoResponse>
}