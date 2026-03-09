package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.domain.Nota
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException

@Service
class ServicioNota(
    val notaRepository: NotaRepository,
    val servicioPaciente: IServicioPaciente,
    val servicioPsicologo: IServicioPsicologo,
) : IServicioNota {


    @Transactional(readOnly = true)
    override fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<NotaResponse>{
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId) ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId) ?: throw IllegalStateException("El paciente no existe")

        if (paciente.psicologoId != psicologo.id) throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")

        val notas = notaRepository.obtenerNotasPacienteParaPsicologo(paciente.id, psicologo.id)

        return notas.map { NotaMapper.toResponse(it) }
    }
    override fun obtenerNotasPaciente(firebaseId: String): NotaResponse?{
        return notaRepository.obtenerByPacienteUsuarioFirebaseId(firebaseId)?.let { NotaMapper.toResponse(it) }
    }

    @Transactional
    override fun crearNota(firebaseId: String, request: NotaRequest): NotaResponse {

        // 1. Obtenemos la ENTIDAD Paciente (¡Necesitamos un método interno en tu ServicioPaciente o usar el Repo!)
        // Asumo que creas este método o usas pacienteRepository directamente.
        val paciente = servicioPaciente.obtenerEntidadPacientePorFirebaseId(firebaseId)

        // 2. Extraemos la ENTIDAD Psicólogo que ya viene dentro del Paciente
        val psicologoAsignado = paciente.psicologo
            ?: throw IllegalStateException("No puedes crear una nota porque no tienes un psicólogo asignado")

        // 3. Ahora sí, el Mapper recibe las ENTIDADES reales para construir la Nota
        val nuevaNota = NotaMapper.toEntity(request, paciente, psicologoAsignado)

        // 4. Guardamos en la base de datos (Esto nos devuelve la Entidad guardada con su ID autogenerado)
        val notaGuardada = notaRepository.save(nuevaNota)

        // 5. ¡Pasamos la Entidad por el Mapper para devolver el DTO final!
        return NotaMapper.toResponse(notaGuardada)
    }
}