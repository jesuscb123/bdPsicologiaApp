package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse

interface IServicioPaciente {
    fun obtenerPacienteFirebaseId(firebaseUsuarioId: String): PacienteResponse?
    fun obtenerPacienteId(id: Long): PacienteResponse?
    fun crearPaciente(usuario: Usuario, pacienteRequest: PacienteRequest): PacienteResponse

    fun obtenerEntidadPacientePorFirebaseId(firebaseId: String): Paciente

    fun buscarPacientesPorNombre(nombreUsuario: String): List<PacienteResponse>

    fun actualizarPsicologo(firebaseUidPaciente: String, psicologoId: Long): PacienteResponse

    /**
     * Devuelve únicamente los pacientes asignados al psicólogo cuyo firebaseUid se pasa.
     * Sustituye al antiguo `obtenerPacientes()` para evitar el IDOR de exponer todos los
     * pacientes a cualquier usuario autenticado.
     */
    fun obtenerPacientesAsignadosA(firebaseUidPsicologo: String): List<PacienteResponse>

    /**
     * Carga un paciente por id de entidad PACIENTES_v2 verificando que el llamante puede leerlo:
     *  - el propio paciente, o
     *  - el psicólogo asignado a ese paciente.
     *
     * Lanza [SecurityException] si el llamante no está autorizado, e [IllegalStateException]
     * si el paciente no existe.
     */
    fun obtenerPacientePorIdConAutorizacion(
        firebaseUidLlamante: String,
        pacienteId: Long
    ): PacienteResponse

    /**
     * Carga un paciente por firebaseUid de usuario verificando la misma regla que
     * [obtenerPacientePorIdConAutorizacion].
     */
    fun obtenerPacientePorFirebaseIdConAutorizacion(
        firebaseUidLlamante: String,
        firebaseUidPaciente: String
    ): PacienteResponse
}
