package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota

interface IServicioNota {
    fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<Nota>

    fun obtenerNotasPaciente(firebaseId: String): Nota?
}