package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.config

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.TareaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * Tras añadir [dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Tarea.aceptadaPorPaciente],
 * las filas antiguas con [Tarea.realizada] ya true quedan con aceptada=false; se corrige una vez al arrancar.
 */
@Component
class ReparacionTareasHistoricas(
    private val tareaRepository: TareaRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        tareaRepository.marcarAceptadasParaTareasYaRealizadas()
    }
}
