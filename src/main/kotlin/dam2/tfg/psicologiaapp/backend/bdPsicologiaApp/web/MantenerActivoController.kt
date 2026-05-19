package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mantener-activo")
class MantenerActivoController(private val usuarioRepository: UsuarioRepository) {

    @GetMapping
    fun mantenerActivo(): ResponseEntity<Map<String, String>> {
        usuarioRepository.count()
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("status" to "ok"))
    }
}
