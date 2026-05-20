package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.notaDto.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.EstadoSyncResponse

interface IServicioNota {
    fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<NotaResponse>

    fun obtenerNotasPaciente(firebaseId: String): List<NotaResponse>

    fun obtenerEstadoNotasPaciente(firebaseUidPaciente: String): EstadoSyncResponse

    fun obtenerEstadoNotasPacienteParaPsicologo(firebaseUidPsicologo: String, pacienteId: Long): EstadoSyncResponse

    fun crearNota(firebaseId: String, notaRequest: NotaRequest): NotaResponse

    fun actualizarNota(firebaseUidPaciente: String, notaId: Long, request: NotaRequest): NotaResponse

    fun eliminarNota(firebaseUidPaciente: String, notaId: Long)
}