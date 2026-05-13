package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class BdPsicologiaAppApplication

fun main(args: Array<String>) {
	runApplication<BdPsicologiaAppApplication>(*args)
}
