package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.PacienteDTO.PacienteRequest

interface IServicioPaciente {
    fun obtenerPacientes(): List<Paciente>
    fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): Paciente?
    fun obtenerPacienteId(id: Long): Paciente?
    fun crearPaciente(firebaseUsuarioId: String, pacienteRequest: PacienteRequest):Paciente?

}