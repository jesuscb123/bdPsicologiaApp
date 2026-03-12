package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse

interface IServicioPaciente {
    fun obtenerPacientes(): List<PacienteResponse>
    fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): PacienteResponse?
    fun obtenerPacienteId(id: Long): PacienteResponse?
    fun crearPaciente(usuario: Usuario, pacienteRequest: PacienteRequest): PacienteResponse

    fun obtenerEntidadPacientePorFirebaseId(firebaseId: String): Paciente

    fun buscarPacientesPorNombre(nombreUsuario: String): List<PacienteResponse>

    fun actualizarPsicologo(firebaseUidPaciente: String, psicologoId: Long): PacienteResponse
}