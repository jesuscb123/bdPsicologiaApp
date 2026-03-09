package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPaciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/api/pacientes")
class PacienteController(
    private val  servicioPaciente: IServicioPaciente,
    private val firebaseService: FirebaseService,
    private val servicioUsuario: IServicioUsuario
) : IController{
    @GetMapping
    fun obtenerPacientes(): List<PacienteResponse>{
        return servicioPaciente.obtenerPacientes()
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPacienteByFirebaseId(firebaseId: String): ResponseEntity<PacienteResponse>{
        val paciente = servicioPaciente.obtenerPacienteFirebaseId(firebaseId)

       return if (paciente != null){
            ResponseEntity.ok(paciente)
        }else{
            ResponseEntity.notFound().build()
       }
    }

    @GetMapping("/id/{id}")
    fun obtenerPacienteById(@PathVariable id: Long?): ResponseEntity<Any> {
        if (id == null ) return ResponseEntity.badRequest().body("El id de paciente no puede ser nulo")

        val paciente = servicioPaciente.obtenerPacienteId(id)
        return if (paciente != null){
            ResponseEntity.ok(paciente)
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun crearPaciente(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @RequestBody pacienteRequest: PacienteRequest
    ): ResponseEntity<Any> {

        return try {
            val pacienteResponse = servicioUsuario.crearUsuario(
                fireBaseUid = usuarioFirebase.uid,
                email = usuarioFirebase.email,
                request = pacienteRequest
            )

            // 2. Retornamos el DTO de respuesta con un 201 Created
            ResponseEntity.created(URI.create("/api/pacientes/${pacienteResponse.id}"))
                .body(pacienteResponse)

        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)

        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error técnico: ${e.message} | Causa: ${e.cause}")
        }
    }
}