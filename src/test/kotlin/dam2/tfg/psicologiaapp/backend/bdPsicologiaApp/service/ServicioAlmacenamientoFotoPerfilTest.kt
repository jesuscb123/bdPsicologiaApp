package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

internal class ServicioAlmacenamientoFotoPerfilTest {

    @TempDir
    lateinit var directorioTemporal: Path

    private fun servicio(basePublica: String = "http://localhost:8080") =
        ServicioAlmacenamientoFotoPerfil(
            directorioRaiz = directorioTemporal.toString(),
            urlPublicaBase = basePublica,
        )

    @Test
    fun `guardar lanza cuando el archivo esta vacio`() {
        assertThrows<IllegalArgumentException> {
            servicio().guardar(ByteArray(0), "image/png")
        }
    }

    @Test
    fun `guardar lanza cuando supera el limite de tamano`() {
        val bytesGrandes = ByteArray(5 * 1024 * 1024 + 1)
        assertThrows<IllegalArgumentException> {
            servicio().guardar(bytesGrandes, "image/jpeg")
        }
    }

    @Test
    fun `guardar persiste fichero y devuelve url publica`() {
        val bytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        val url = servicio().guardar(bytes, "image/png")

        assertTrue(url.startsWith("http://localhost:8080/api/archivos/perfiles/"))
        assertTrue(url.endsWith(".png"))
        val nombreFichero = url.substringAfterLast('/')
        assertTrue(Files.exists(directorioTemporal.resolve(nombreFichero)))
    }

    @Test
    fun `guardar usa base publica personalizada cuando se proporciona`() {
        val bytes = byteArrayOf(1, 2, 3)
        val url = servicio().guardar(bytes, "image/jpeg", "https://api.ejemplo.com/")

        assertTrue(url.startsWith("https://api.ejemplo.com/api/archivos/perfiles/"))
        assertTrue(url.endsWith(".jpg"))
    }
}
