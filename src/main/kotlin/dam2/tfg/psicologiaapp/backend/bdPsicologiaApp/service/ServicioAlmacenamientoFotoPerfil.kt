package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * Guarda imágenes de perfil en disco y devuelve la URL pública servida por [ArchivoPerfilController].
 */
@Service
class ServicioAlmacenamientoFotoPerfil(
    @Value("\${app.fotos-perfil.directorio}") private val directorioRaiz: String,
    @Value("\${app.fotos-perfil.url-publica-base}") private val urlPublicaBase: String,
) {

    fun guardar(bytes: ByteArray, tipoContenido: String?): String {
        require(bytes.isNotEmpty()) { "El archivo está vacío" }
        require(bytes.size <= LIMITE_BYTES_IMAGEN_PERFIL) {
            "La imagen supera el tamaño máximo (${LIMITE_BYTES_IMAGEN_PERFIL / (1024 * 1024)} MB)"
        }
        val extension = resolverExtension(tipoContenido)
        val nombreFichero = "${UUID.randomUUID()}.$extension"
        val directorioAbsoluto = Paths.get(directorioRaiz).toAbsolutePath().normalize()
        Files.createDirectories(directorioAbsoluto)
        val rutaFichero = directorioAbsoluto.resolve(nombreFichero)
        Files.write(rutaFichero, bytes)
        val base = urlPublicaBase.trimEnd('/')
        return "$base/api/archivos/perfiles/$nombreFichero"
    }

    private fun resolverExtension(tipoContenido: String?): String {
        val tipo = tipoContenido?.lowercase()?.trim().orEmpty()
        return when {
            tipo.contains("png") -> "png"
            tipo.contains("webp") -> "webp"
            tipo.contains("gif") -> "gif"
            tipo.startsWith("image/") -> "jpg"
            else -> "jpg"
        }
    }

    companion object {
        private const val LIMITE_BYTES_IMAGEN_PERFIL = 5 * 1024 * 1024
    }
}
