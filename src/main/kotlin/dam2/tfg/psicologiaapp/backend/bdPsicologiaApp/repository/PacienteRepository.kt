package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Paciente
import org.springframework.data.jpa.repository.JpaRepository

interface PacienteRepository : JpaRepository<Paciente, Long> {
}