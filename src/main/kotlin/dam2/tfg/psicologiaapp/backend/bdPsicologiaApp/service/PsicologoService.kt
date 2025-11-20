package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PsicologoService(
    val psicologoRepository: PsicologoRepository
) {
    @Transactional
    fun obtenerPsicologos(): List<Psicologo> {
        return psicologoRepository.findAll()
    }

    fun obtenerPsicologo(firebaseUidUsuario: String): Psicologo?{
        return psicologoRepository.findByIdFirebaseUsuario(firebaseUidUsuario)
    }

    fun crearPsicologo(){

    }
}