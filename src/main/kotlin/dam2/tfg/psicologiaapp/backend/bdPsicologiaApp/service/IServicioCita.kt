package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.CitaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.citaDTO.DisponibilidadResponse
import java.time.LocalDate

interface IServicioCita {
    fun obtenerDisponibilidadDia(firebaseUidPaciente: String, fecha: LocalDate, zonaHoraria: String): DisponibilidadResponse

    fun reservarCita(firebaseUidPaciente: String, request: CitaCrearRequest): CitaResponse

    fun cancelarCita(firebaseUidPaciente: String, citaId: Long): CitaResponse

    fun obtenerMisCitasPaciente(firebaseUidPaciente: String): List<CitaResponse>

    fun obtenerMisCitasPsicologo(firebaseUidPsicologo: String): List<CitaResponse>
}

