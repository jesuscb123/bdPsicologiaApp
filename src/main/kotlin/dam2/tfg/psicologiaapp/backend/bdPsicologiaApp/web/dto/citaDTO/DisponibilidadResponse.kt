package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO

import java.time.LocalDate
import java.time.LocalTime

data class DisponibilidadResponse(
    val fecha: LocalDate,
    val zonaHoraria: String,
    val horasDisponibles: List<LocalTime>
)

