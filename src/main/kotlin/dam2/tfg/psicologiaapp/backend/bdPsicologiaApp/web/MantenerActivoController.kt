package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.IServicioUsuario
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/mantener-activo")
class MantenerActivoController(private val servicioUsuario: IServicioUsuario) {

    @GetMapping
    fun mantenerActivo(): ResponseEntity<Map<String, String>> {
        servicioUsuario.existeCorreo("ping@mantener-activo.internal")
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}
