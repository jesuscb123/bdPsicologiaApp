package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.repository.NotaRepository
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.deteccionRiesgo.IServicioDeteccionRiesgo
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.EstadoSyncResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaRequest
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.NotaDTO.NotaResponse
import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.mapper.NotaMapper
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.lang.IllegalStateException

@Service
class ServicioNota(
    val notaRepository: NotaRepository,
    val servicioPaciente: IServicioPaciente,
    val servicioPsicologo: IServicioPsicologo,
    val servicioDeteccionRiesgo: IServicioDeteccionRiesgo,
) : IServicioNota {

    private val log = LoggerFactory.getLogger(ServicioNota::class.java)


    @Transactional(readOnly = true)
    override fun obtenerNotasPacienteParaPsicologo(firebaseId: String, pacienteId: Long): List<NotaResponse>{
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseId) ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId) ?: throw IllegalStateException("El paciente no existe")

        // PsicologoResponse.id es el usuario; psicologoId del paciente y el repositorio usan el id de entidad PSICOLOGOS.
        if (paciente.psicologoId != psicologo.idEntidadPsicologo) {
            throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")
        }

        val notas = notaRepository.obtenerNotasPacienteParaPsicologo(
            paciente.idPaciente,
            psicologo.idEntidadPsicologo,
        )

        return notas.map { NotaMapper.toResponse(it) }
    }
    override fun obtenerNotasPaciente(firebaseId: String): List<NotaResponse>{
        val notas = notaRepository.obtenerNotasByPacienteUsuarioFirebaseId(firebaseId)

        return notas.map { NotaMapper.toResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun obtenerEstadoNotasPaciente(firebaseUidPaciente: String): EstadoSyncResponse {
        val estado = notaRepository.obtenerEstadoNotasPaciente(firebaseUidPaciente)
        return EstadoSyncResponse(
            ultimaModificacion = estado.ultimaModificacion,
            total = estado.total
        )
    }

    @Transactional(readOnly = true)
    override fun obtenerEstadoNotasPacienteParaPsicologo(
        firebaseUidPsicologo: String,
        pacienteId: Long
    ): EstadoSyncResponse {
        val psicologo = servicioPsicologo.obtenerPsicologoFirebaseId(firebaseUidPsicologo)
            ?: throw IllegalStateException("El psicólogo no existe")

        val paciente = servicioPaciente.obtenerPacienteId(pacienteId)
            ?: throw IllegalStateException("El paciente no existe")

        if (paciente.psicologoId != psicologo.idEntidadPsicologo) {
            throw SecurityException("No tienes permiso para acceder a las notas de este paciente.")
        }

        val estado = notaRepository.obtenerEstadoNotasPacienteParaPsicologo(
            pacienteId = paciente.idPaciente,
            psicologoId = psicologo.idEntidadPsicologo
        )

        return EstadoSyncResponse(
            ultimaModificacion = estado.ultimaModificacion,
            total = estado.total
        )
    }

    @Transactional
    override fun crearNota(firebaseId: String, notaRequest: NotaRequest): NotaResponse {

        val paciente = servicioPaciente.obtenerEntidadPacientePorFirebaseId(firebaseId)

        val psicologoAsignado = paciente.psicologo
            ?: throw IllegalStateException("No puedes crear una nota porque no tienes un psicólogo asignado")

        val nuevaNota = NotaMapper.toEntity(notaRequest, paciente, psicologoAsignado)

        val notaGuardada = notaRepository.save(nuevaNota)

        // Disparamos la detección de riesgo tras el commit. El propio servicio es @Async, así
        // que aunque registremos el callback aquí, el paciente recibe respuesta inmediata.
        paciente.id?.let { lanzarDeteccionRiesgoTrasCommit(it) }

        return NotaMapper.toResponse(notaGuardada)
    }

    @Transactional
    override fun actualizarNota(firebaseUidPaciente: String, notaId: Long, request: NotaRequest): NotaResponse {
        val nota = notaRepository.findByIdOrNull(notaId)
            ?: throw IllegalStateException("La nota no existe")

        if (nota.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para actualizar esta nota")
        }

        nota.asunto = request.asunto
        nota.descripcion = request.descripcion
        val actualizada = notaRepository.save(nota)

        // Misma lógica que crear: el contenido cambió, conviene re-evaluar el conjunto.
        nota.paciente.id?.let { lanzarDeteccionRiesgoTrasCommit(it) }

        return NotaMapper.toResponse(actualizada)
    }

    /**
     * Registra un callback que, después de hacer commit con éxito, invoca la detección asíncrona
     * de riesgo. Si lanzáramos la detección dentro de la propia tx y luego hubiera rollback,
     * estaríamos alertando al psicólogo de una nota que en realidad no se persistió.
     *
     * Si no hay sincronización transaccional activa (caso atípico), invocamos directamente: como
     * el método es `@Async` se desacopla del hilo actual de todos modos.
     */
    private fun lanzarDeteccionRiesgoTrasCommit(pacienteId: Long) {
        val ejecutar = {
            try {
                servicioDeteccionRiesgo.evaluarRiesgoUltimasNotasAsync(pacienteId)
            } catch (e: Exception) {
                log.warn("No se pudo lanzar la detección de riesgo para paciente {}: {}", pacienteId, e.message)
            }
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    ejecutar()
                }
            })
        } else {
            ejecutar()
        }
    }

    @Transactional
    override fun eliminarNota(firebaseUidPaciente: String, notaId: Long) {
        val nota = notaRepository.findByIdOrNull(notaId)
            ?: throw IllegalStateException("La nota no existe")

        if (nota.paciente.usuario.firebaseUid != firebaseUidPaciente) {
            throw SecurityException("No tienes permiso para eliminar esta nota")
        }

        notaRepository.delete(nota)
    }
}