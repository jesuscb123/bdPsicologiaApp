package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Usuario
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PacienteResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.usuarioDTO.PsicologoResponse

interface IServicioPsicologo {
    fun obtenerPsicologos(): List<PsicologoResponse>

    fun obtenerPsicologoFirebaseId(firebaseUsuarioId: String): PsicologoResponse?

    fun crearPsicologo(usuario: Usuario, psicologoRequest: PsicologoRequest): PsicologoResponse

    fun obtenerPsicologoId(id: Long): PsicologoResponse?

    fun obtenerEntidadPsicologo(id: Long): Psicologo

    fun buscarPsicologosPorNombre(nombreUsuario: String): List<PsicologoResponse>

    fun obtenerPacientesPorFirebaseId(firebaseUidPsicologo: String): List<PacienteResponse>

    fun actualizarDescripcion(firebaseUidPsicologo: String, descripcion: String?): PsicologoResponse

    /**
     * Carga un psicólogo por id de entidad PSICOLOGOS verificando que el llamante puede leerlo:
     *  - el propio psicólogo, o
     *  - un paciente con relación establecida (paciente.psicologo_id apunta a este psicólogo).
     *
     * Lanza [SecurityException] si el llamante no está autorizado e [IllegalStateException]
     * si el psicólogo no existe.
     */
    fun obtenerPsicologoPorIdConAutorizacion(
        firebaseUidLlamante: String,
        psicologoId: Long
    ): PsicologoResponse

    /**
     * Carga un psicólogo por firebaseUid de usuario aplicando la misma regla que
     * [obtenerPsicologoPorIdConAutorizacion].
     */
    fun obtenerPsicologoPorFirebaseIdConAutorizacion(
        firebaseUidLlamante: String,
        firebaseUidPsicologo: String
    ): PsicologoResponse
}