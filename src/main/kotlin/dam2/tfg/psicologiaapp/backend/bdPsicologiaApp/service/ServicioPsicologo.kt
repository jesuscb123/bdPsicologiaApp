package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PsicologoRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPsicologo(
   private val psicologoRepository: PsicologoRepository,
    private val servicioUsuario: IServicioUsuario
) : IServicioPsicologo{
    @Transactional
    override fun obtenerPsicologos(): List<Psicologo> {
        return psicologoRepository.findAll()
    }

    @Transactional
    override fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): Psicologo?{
        return psicologoRepository.findByIdFirebaseUsuario(firebaseUsuarioId)
    }

    @Transactional
    override fun obtenerPsicologoId(id: Long): Psicologo? {
        return psicologoRepository.findById(id).orElse(null)
    }

    @Transactional
    override fun crearPsicologo(firebaseUidUsuario: String, psicologoRequest: PsicologoRequest): Psicologo?{
        val usuarioExiste = servicioUsuario.obtenerUsuarioByFireBaseId(firebaseUidUsuario) ?: throw IllegalStateException("No se puede crear un perfil para un usuario inexistente: ${firebaseUidUsuario} ")

        if (psicologoRepository.existsByUsuario(usuarioExiste)){
            return null
        }else{
            val nuevoPsicologo = Psicologo(
                usuario = usuarioExiste,
                numeroColegiado = psicologoRequest.numeroColegiado,
                especialidad = psicologoRequest.especialidad
            )
            return psicologoRepository.save(nuevoPsicologo)
        }
    }
}