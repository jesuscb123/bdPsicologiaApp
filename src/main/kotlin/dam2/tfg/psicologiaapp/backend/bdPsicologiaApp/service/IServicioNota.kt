package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse

interface IServicioNota {
    fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<NotaResponse>

    fun obtenerNotasPaciente(firebaseId: String): NotaResponse?

    fun crearNota(firebaseId: String, notaRequest: NotaRequest): Nota
}