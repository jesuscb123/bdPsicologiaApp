package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto

import java.time.LocalDateTime

data class EstadoSyncResponse(
    val ultimaModificacion: LocalDateTime?,
    val total: Long
)

