package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web

import java.nio.file.Files
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.file.Path

internal class ArchivoPerfilControllerTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            ArchivoPerfilController(tempDir.toString())
        ).build()
    }

    @Test
    fun `GET fichero existente devuelve 200`() {
        val nombre = "foto_test.jpg"
        Files.write(tempDir.resolve(nombre), byteArrayOf(1, 2, 3))

        mockMvc.perform(get("/api/archivos/perfiles/$nombre"))
            .andExpect(status().isOk)
            .andExpect(header().string("Cache-Control", "public, max-age=86400"))
    }

    @Test
    fun `GET fichero inexistente devuelve 404`() {
        mockMvc.perform(get("/api/archivos/perfiles/no_existe.jpg"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET nombre con path traversal devuelve 404`() {
        mockMvc.perform(get("/api/archivos/perfiles/../secret.txt"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `GET nombre con barras devuelve 404`() {
        mockMvc.perform(get("/api/archivos/perfiles/sub/dir.jpg"))
            .andExpect(status().isNotFound)
    }
}
