package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoRequest

interface IServicioPsicologo {
    fun obtenerPsicologos(): List<Psicologo>

    fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): Psicologo?

    fun crearPsicologo(firebaseUidUsuario: String, psicologoRequest: PsicologoRequest): Psicologo?

    fun obtenerPsicologoId(id: Long): Psicologo?
}