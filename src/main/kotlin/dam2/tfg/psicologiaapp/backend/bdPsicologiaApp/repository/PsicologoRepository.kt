package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Psicologo
import org.springframework.data.jpa.repository.JpaRepository

interface PsicologoRepository : JpaRepository<Psicologo, Long> {
}