package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest

interface IServicioNota {
    fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<Nota>

    fun obtenerNotasPaciente(firebaseId: String): Nota?

    fun crearNota(firebaseId: String, notaRequest: NotaRequest): Nota
}