package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import org.springframework.data.jpa.repository.JpaRepository

interface NotaRepository : JpaRepository<Nota, Long> {
}