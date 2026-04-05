package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Sirve las imágenes de perfil almacenadas en disco (URL pública para Coil / navegadores).
 */
@RestController
@RequestMapping("/api/archivos/perfiles")
class ArchivoPerfilController(
    @Value("\${app.fotos-perfil.directorio}") private val directorioRaiz: String,
) {

    @GetMapping("/{nombreFichero}")
    fun obtenerFichero(@PathVariable nombreFichero: String): ResponseEntity<Resource> {
        if (!esNombreFicheroSeguro(nombreFichero)) {
            return ResponseEntity.notFound().build()
        }
        val directorioBase = Paths.get(directorioRaiz).toAbsolutePath().normalize()
        val ruta = directorioBase.resolve(nombreFichero).normalize()
        if (!ruta.startsWith(directorioBase) || !Files.isRegularFile(ruta)) {
            return ResponseEntity.notFound().build()
        }
        val recurso: Resource = UrlResource(ruta.toUri())
        if (!recurso.exists() || !recurso.isReadable) {
            return ResponseEntity.notFound().build()
        }
        val tipoContenido = Files.probeContentType(ruta)?.let { MediaType.parseMediaType(it) }
            ?: MediaType.APPLICATION_OCTET_STREAM
        return ResponseEntity.ok()
            .contentType(tipoContenido)
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
            .body(recurso)
    }

    private fun esNombreFicheroSeguro(nombre: String): Boolean {
        if (nombre.isBlank() || nombre.length > 200) return false
        if (nombre.contains("..") || nombre.contains('/') || nombre.contains('\\')) return false
        return nombre.matches(Regex("^[a-zA-Z0-9_.-]+$"))
    }
}
