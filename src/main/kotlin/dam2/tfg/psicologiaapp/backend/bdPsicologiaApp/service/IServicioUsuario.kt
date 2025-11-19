package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario

interface IServicioUsuario {
    fun encontrarByFirebaseUid(idFirebase: String) : Usuario?

    fun crearUsuario(uid: String, email: String): Usuario

    fun crearPsicologo(usuarioId: Long, numeroColegiado: String): Psicologo

    fun crearPaciente(usuarioId: Long, psicologoId: Long): Paciente
}