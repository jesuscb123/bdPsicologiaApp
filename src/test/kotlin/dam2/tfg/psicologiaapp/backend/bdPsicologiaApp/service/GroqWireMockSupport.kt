package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config.GroqProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient

internal object GroqWireMockSupport {

    fun crearServidor(): WireMockServer =
        WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

    fun propiedadesGroq(servidor: WireMockServer): GroqProperties = GroqProperties(
        apiKey = "test-groq-key",
        baseUrl = servidor.baseUrl(),
        modelo = "llama-test",
        maxTokens = 300,
        temperatura = 0.2,
    )

    fun clienteGroq(propiedades: GroqProperties): RestClient {
        val fabrica = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(5_000)
            setReadTimeout(5_000)
        }
        return RestClient.builder()
            .baseUrl(propiedades.baseUrl)
            .requestFactory(fabrica)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeaders { cabeceras -> cabeceras.setBearerAuth(propiedades.apiKey) }
            .build()
    }

    fun stubGroqChatCompletion(servidor: WireMockServer, contenidoMensaje: String) {
        val cuerpo = """
            {
              "id": "chatcmpl-test",
              "model": "llama-test",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": ${escapeJsonString(contenidoMensaje)}
                  },
                  "finish_reason": "stop"
                }
              ]
            }
        """.trimIndent()
        servidor.stubFor(
            post(urlEqualTo("/chat/completions"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(cuerpo),
                ),
        )
    }

    fun stubGroqError(servidor: WireMockServer, httpStatus: Int) {
        servidor.stubFor(
            post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse().withStatus(httpStatus).withBody("""{"error":"fallo"}""")),
        )
    }

    private fun escapeJsonString(valor: String): String {
        val escapado = valor
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escapado\""
    }
}
