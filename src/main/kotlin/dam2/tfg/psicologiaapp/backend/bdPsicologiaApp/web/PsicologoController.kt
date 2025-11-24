package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.FirebaseService
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioPsicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.psicologoDTO.PsicologoResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.PsicologoMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/psicologos")
class PsicologoController(
    private val servicioPsicologo: IServicioPsicologo,
    private val firebaseService: FirebaseService
) {
    @GetMapping
    fun obtenerPsicologos(): List<PsicologoResponse> {
        return servicioPsicologo.obtenerPsicologos().map(PsicologoMapper::toResponse)
    }

    @GetMapping("/firebaseId/{firebaseId}")
    fun obtenerPsicologoByFirebaseId(firebaseId: String): ResponseEntity<PsicologoResponse>? {
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId)
        return if (psicologo != null) {
            ResponseEntity.ok(PsicologoMapper.toResponse(psicologo))
        } else {
            null
        }
    }

    @GetMapping("/id/{id}")
    fun obtenerPsicologoById(@PathVariable id: Long?): ResponseEntity<Any> {

        if (id == null) {
            return ResponseEntity.badRequest().body("El ID del psicólogo no puede ser nulo.")
        }

        return if (servicioPsicologo.obtenerPsicologoId(id) != null){
            ResponseEntity.ok(PsicologoMapper.toResponse(servicioPsicologo.obtenerPsicologoId(id)!!))
        }else{
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun crearPsicologo(
        @RequestHeader("Authorization") authorizationHeader: String,
        @RequestBody psicologoRequest: PsicologoRequest
    ): ResponseEntity<Any>{
        try{
            val usuarioFirebase = firebaseService.getUserFromToken(authorizationHeader)
                ?: return errorTokenExpirado()

          return guardarPsicologo(usuarioFirebase, psicologoRequest)
        }catch (e: Exception){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor")
        }
    }


    private fun guardarPsicologo(usuarioFirebase: FirebaseUserData, psicologoRequest: PsicologoRequest): ResponseEntity<Any>{
        val psicologoGuardado = servicioPsicologo.crearPsicologo(usuarioFirebase.uid, psicologoRequest)

        return if (psicologoGuardado !=  null){
            val psicologoResponse = PsicologoMapper.toResponse(psicologoGuardado)
            ResponseEntity.created(URI.create("/api/psicologos/${psicologoResponse.id}")).body(psicologoResponse)
        }else{
            errorPsicologoExiste(usuarioFirebase)
        }

    }

    // FUNCIONES ERRORES
    private fun errorTokenExpirado(): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autorización inválido o expirado.")

    }

    private fun errorPsicologoExiste(usuarioFirebase: FirebaseUserData): ResponseEntity<Any>{
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El psicólogo con ${usuarioFirebase.uid} ya existe.")
    }
}