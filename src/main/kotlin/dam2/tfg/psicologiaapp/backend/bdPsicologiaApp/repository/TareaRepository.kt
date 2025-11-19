package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea
import org.springframework.data.jpa.repository.JpaRepository

interface TareaRepository : JpaRepository<Tarea, Long> {
}