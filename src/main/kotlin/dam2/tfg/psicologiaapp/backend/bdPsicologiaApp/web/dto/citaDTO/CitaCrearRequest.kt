package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO

import java.time.OffsetDateTime

data class CitaCrearRequest(
    val inicio: OffsetDateTime,
    /** Zona horaria IANA, p. ej. "Europe/Madrid". */
    val zonaHoraria: String
)

