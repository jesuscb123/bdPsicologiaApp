package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

import dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.iaDTO.ResumenIaResponse

/**
 * Contrato del servicio que genera un resumen IA de las últimas notas de un paciente.
 *
 * El servicio:
 *  - Valida que el paciente pertenece al psicólogo autenticado (reglas idénticas a
 *    [IServicioNota.obtenerNotasPacienteParaPsicologo]).
 *  - Anonimiza el contenido antes de enviarlo a Groq (solo asunto + descripción).
 *  - Devuelve siempre un mensaje seguro al cliente; los detalles técnicos quedan en logs.
 *
 * Errores documentados:
 *  - [SecurityException] si el paciente no pertenece al psicólogo.
 *  - [IllegalStateException] con mensaje `"Sin notas"` si el paciente no tiene notas
 *    que analizar (el controller debe mapearlo a 404).
 *  - [dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service.ia.ResumenIaServicioNoDisponibleException]
 *    (también un [IllegalStateException]) si la API key de Groq no está configurada o
 *    si Groq responde con un fallo; el controller debe mapearlo a 503.
 */
interface IServicioResumenIa {
    fun generarResumenNotasPaciente(uidPsicologo: String, pacienteId: Long): ResumenIaResponse
}
