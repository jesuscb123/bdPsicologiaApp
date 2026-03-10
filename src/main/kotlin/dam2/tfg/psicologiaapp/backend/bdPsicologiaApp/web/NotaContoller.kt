package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioNota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/notas")
class NotaContoller(
    private val servicioNota: IServicioNota
) {
    @GetMapping("/psicologo/firebaseId/{firebaseId}/paciente/{pacienteId}")
    fun obtenerNotasParaPsicologo(
        @PathVariable firebaseId: String,
        @PathVariable pacienteId: Long
    ): ResponseEntity<List<NotaResponse>> {

        val notas = servicioNota.obtenerNotasPacienteParaPsicologo(firebaseId, pacienteId)

        return if (notas.isNotEmpty()) {
            ResponseEntity.ok(notas)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/paciente/firebaseId/{firebaseId}")
    fun obtenerMisNotas(
        @PathVariable firebaseId: String
    ): ResponseEntity<NotaResponse> {

        val nota = servicioNota.obtenerNotasPaciente(firebaseId)

        return if (nota != null) {
            ResponseEntity.ok(nota)
        } else {
            ResponseEntity.notFound().build()
        }
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