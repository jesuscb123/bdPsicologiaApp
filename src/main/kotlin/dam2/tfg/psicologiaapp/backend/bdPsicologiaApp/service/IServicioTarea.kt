package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRealizadaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaActualizarRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaCrearRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.tareaDTO.TareaResponse

interface IServicioTarea {
    fun crearTarea(firebaseUidPsicologo: String, pacienteId: Long, request: TareaCrearRequest): TareaResponse

    fun obtenerTareasPaciente(firebaseUidPaciente: String): List<TareaResponse>

    fun obtenerTareasPacienteParaPsicologo(firebaseUidPsicologo: String, pacienteId: Long): List<TareaResponse>

    fun actualizarRealizada(firebaseUidPaciente: String, tareaId: Long, request: TareaActualizarRealizadaRequest): TareaResponse

    fun actualizarTarea(firebaseUidPsicologo: String, tareaId: Long, request: TareaActualizarRequest): TareaResponse
}

