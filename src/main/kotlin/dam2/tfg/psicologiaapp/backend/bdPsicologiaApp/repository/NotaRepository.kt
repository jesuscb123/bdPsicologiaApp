package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import org.springframework.data.jpa.repository.JpaRepository

interface NotaRepository : JpaRepository<Nota, Long> {
    fun obtenerNotasPacienteParaPsicologo(paciente: Paciente, psicologo: Psicologo): List<Nota>

    fun obtenerByPacienteUsuarioFirebaseId(fireBaseId: String): Nota?
}