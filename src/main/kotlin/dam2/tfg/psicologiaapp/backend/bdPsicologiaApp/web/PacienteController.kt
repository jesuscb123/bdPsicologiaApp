package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPaciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/pacientes")
class PacienteController(
    private val  servicioPaciente: IServicioPaciente
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
}