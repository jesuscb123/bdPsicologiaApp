package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.FirebaseUserData
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
import org.apache.coyote.Response
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/notas")
class NotaContoller(
    private val servicioNota: IServicioNota
) {
    @GetMapping("/pacientes/{pacienteId}")
    fun obtenerNotasParaPsicologo(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData,
        @PathVariable pacienteId: Long
    ): ResponseEntity<List<NotaResponse>> {
        val notas = servicioNota.obtenerNotasPacienteParaPsicologo(usuarioFirebase.uid, pacienteId)

        return if (notas.isNotEmpty()) {
            ResponseEntity.ok(notas)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping
    fun obtenerMisNotas(
        @AuthenticationPrincipal usuarioFirebase: FirebaseUserData
    ): ResponseEntity<List<NotaResponse>> {

        val notas = servicioNota.obtenerNotasPaciente(usuarioFirebase.uid)

        return if (notas.isNotEmpty()) ResponseEntity.ok(notas) else ResponseEntity.noContent().build()
    }

    @PostMapping("/paciente/firebaseId/{firebaseId}")
    fun crearNota(
        @PathVariable firebaseId: String,
        @RequestBody request: NotaRequest
    ): ResponseEntity<NotaResponse> {

        return try {
            val notaGuardada = servicioNota.crearNota(firebaseId, request)

            ResponseEntity.created(URI.create("/api/notas/${notaGuardada.id}")).body(notaGuardada)

        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().build()
        }
    }

}