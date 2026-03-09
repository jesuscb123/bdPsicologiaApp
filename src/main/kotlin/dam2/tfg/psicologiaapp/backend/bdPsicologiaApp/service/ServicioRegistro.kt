package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.google.firebase.auth.FirebaseToken
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.UsuarioRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ServicioRegistro(
    private val usuarioService: ServicioUsuario,
    private val pacienteService: ServicioPaciente,
    private val psicologoService: ServicioPsicologo
) : IServicioRegistro {

    @Transactional
    override fun registrarTodo(usuarioFirebase: FirebaseUserData, request: UsuarioRequest): Usuario {
       val usuarioBase =  usuarioService.crearUsuario(
            usuarioFirebase.uid,
            usuarioFirebase.email!!,
            request
        )

        when (request.rol.uppercase()) {
            "PACIENTE" -> {
                val pacienteReq = PacienteRequest(psicologoId = null)

                pacienteService.crearPaciente(usuarioFirebase.uid, pacienteReq)
            }
            "PSICOLOGO" -> {
                val psicologoReq = PsicologoRequest(
                    numeroColegiado = request.numeroColegiado
                        ?: throw IllegalStateException("Número de colegiado requerido"),
                    especialidad = request.especialidad ?: throw IllegalStateException("especialidad requerida")
                )
                psicologoService.crearPsicologo(usuarioFirebase.uid, psicologoReq)
            }
            else -> throw IllegalStateException("Rol no reconocido")
        }
        return usuarioBase
    }
}
