package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository

class ServicioUsuario(val usuarioRepository: UsuarioRepository) : IServicioUsuario {
    override fun encontrarByFirebaseUid(idFirebase: String): Usuario? {
        return usuarioRepository.encontrarByFirebaseUid(idFirebase)
    }

    override fun crearUsuario(uid: String, email: String): Usuario {
        TODO("Not yet implemented")
    }

    override fun crearPsicologo(usuarioId: Long, numeroColegiado: String): Psicologo {
        TODO("Not yet implemented")
    }

    override fun crearPaciente(usuarioId: Long, psicologoId: Long): Paciente {
        TODO("Not yet implemented")
    }


}