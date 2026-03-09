package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest

interface IServicioPsicologo {
    fun obtenerPsicologos(): List<PsicologoResponse>

    fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): Psicologo?

    fun crearPsicologo(usuario: Usuario, psicologoRequest: PsicologoRequest): PsicologoResponse

    fun obtenerPsicologoId(id: Long): Psicologo?
}