package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface NotaRepository : JpaRepository<Nota, Long> {
    @Query("SELECT n FROM Nota n WHERE n.paciente.id = :pacienteId AND n.psicologo.id = :psicologoId")
    fun obtenerNotasPacienteParaPsicologo(
        @Param("pacienteId") pacienteId: Long,
        @Param("psicologoId") psicologoId: Long
    ): List<Nota>

    @Query("SELECT n FROM Nota n WHERE n.paciente.usuario.firebaseUid = :fireBaseId")
    fun obtenerByPacienteUsuarioFirebaseId(fireBaseId: String): Nota?


}