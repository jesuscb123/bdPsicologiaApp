package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Rol
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPaciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PacienteMapper
import org.apache.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

class PacienteController(
    private val  servicioPaciente: IServicioPaciente,
    private val firebaseService: FirebaseService
) : IController{
    @GetMapping
    fun obtenerPacientes(): List<PacienteResponse>{
        return servicioPaciente.obtenerPacientes().map(PacienteMapper::toResponse)
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPacienteByFirebaseId(firebaseId: String): ResponseEntity<PacienteResponse>{
        val paciente = servicioPaciente.obtenerPacienteFirebaseId(firebaseId)

       return if (paciente != null){
            ResponseEntity.ok(PacienteMapper.toResponse(paciente))
        }else{
            ResponseEntity.notFound().build()
       }
    }

    @GetMapping("/id/{id}")
    fun obtenerPacienteById(@PathVariable id: Long?): Any {
        if (id == null ) return ResponseEntity.badRequest().body("El id de paciente no puede ser nulo")

        val paciente = servicioPaciente.obtenerPacienteId(id)
        return if (paciente != null){
            ResponseEntity.ok(PacienteMapper.toResponse(paciente))
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun crearPaciente(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody pacienteRequest: PacienteRequest
    ): ResponseEntity<Any>{
        try{
            val usuarioFirebase = firebaseService.getUserFromToken(authorizationHeader)
                ?: return errorTokenExpirado()

            return guardarPaciente(usuarioFirebase, pacienteRequest)
        }catch (e: Exception){
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body("Error interno del servidor.")
        }
    }

    private fun guardarPaciente(usuarioFirebase: FirebaseUserData, pacienteRequest: PacienteRequest): ResponseEntity<Any>{
        val pacienteGuardado = servicioPaciente.crearPaciente(usuarioFirebase.uid, pacienteRequest)

        return if (pacienteGuardado != null){
            val pacienteReponse = PacienteMapper.toResponse(pacienteGuardado)
            ResponseEntity.created(URI.create("/api/pacientes/${pacienteReponse.id}"))
                .body(pacienteReponse)
        }else{
            errorExiste(usuarioFirebase, Rol.PACIENTE)
        }
    }
}