package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.UsuarioRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.UsuarioMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioUsuario(val usuarioRepository: UsuarioRepository) : IServicioUsuario {
    override fun obtenerUsuarioByFireBaseId(idFirebase: String): Usuario? {
        return usuarioRepository.encontrarByFirebaseUid(idFirebase)
    }

    @Transactional(readOnly = true)
    override fun obtenerUsuarios(): List<Usuario> {
        return usuarioRepository.findAll()
    }

    @Transactional
    override fun crearUsuario(fireBaseUid: String, email: String, request: UsuarioRequest): Usuario {
        val existeUsuario = usuarioRepository.encontrarByFirebaseUid(fireBaseUid)
        if (existeUsuario != null){
            return existeUsuario
        }else{
            require(email.isNotEmpty())
            val nuevoUsuario = UsuarioMapper.toEntity(request, fireBaseUid, email)
            return usuarioRepository.save(nuevoUsuario)
        }
    }
}