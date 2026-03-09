package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioPsicologo(
   private val psicologoRepository: PsicologoRepository,
    private val servicioUsuario: IServicioUsuario
) : IServicioPsicologo{
    @Transactional
    override fun obtenerPsicologos(): List<PsicologoResponse> {
        val psicologos = psicologoRepository.findAll()

        return psicologos.map { PsicologoMapper.toResponse(it) }
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
    override fun crearPsicologo(usuario: Usuario, psicologoRequest: PsicologoRequest): PsicologoResponse {

        if (psicologoRepository.existsByUsuario(usuario)){
            throw IllegalStateException("El usuario ${usuario.nombreUsuario} ya es psicólogo")
        }else{
            val nuevoPsicologo = Psicologo(
                usuario = usuario,
                numeroColegiado = psicologoRequest.numeroColegiado,
                especialidad = psicologoRequest.especialidad
            )
           val psicologo = psicologoRepository.save(nuevoPsicologo)

            return PsicologoMapper.toResponse(psicologo)
        }
    }
}