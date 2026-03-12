package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.security

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PacienteRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.PsicologoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Resuelve los roles del usuario en BD por firebaseUid.
 * Usado por el filtro de seguridad para asignar autoridades.
 */
@Service
class ServicioRoles(
    private val pacienteRepository: PacienteRepository,
    private val psicologoRepository: PsicologoRepository
) {
    @Transactional(readOnly = true)
    fun obtenerRolesPorFirebaseUid(firebaseUid: String): List<String> {
        val roles = mutableListOf<String>()
        if (pacienteRepository.findByIdFirebaseUsuario(firebaseUid) != null) {
            roles.add(ROL_PACIENTE)
        }
        if (psicologoRepository.findByIdFirebaseUsuario(firebaseUid) != null) {
            roles.add(ROL_PSICOLOGO)
        }
        return roles
    }

    companion object {
        const val ROL_PACIENTE = "ROLE_PACIENTE"
        const val ROL_PSICOLOGO = "ROLE_PSICOLOGO"
    }
}
