package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NotaRepository : JpaRepository<Nota, Long> {
    @Query("SELECT n FROM Nota n WHERE n.paciente = :paciente AND n.psicologo = :psicologo")
    fun obtenerNotasPacienteParaPsicologo(paciente: Paciente, psicologo: Psicologo): List<Nota>

    @Query("SELECT n FROM Nota n WHERE n.paciente.usuario.firebaseUid = :fireBaseId")
    fun obtenerByPacienteUsuarioFirebaseId(fireBaseId: String): Nota?
}