package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.service

interface IServicioFcmToken {

    /**
     * Registra (o actualiza) el token FCM del usuario autenticado.
     * Si el token ya existía con otro propietario, se reasigna al usuario actual
     * (escenario típico al cambiar de cuenta en el mismo dispositivo).
     */
    fun registrarToken(
        firebaseUidUsuario: String,
        token: String,
        instalacionId: String?,
        plataforma: String?,
    )

    /** Elimina el token concreto del usuario autenticado (cierre de sesión / logout). */
    fun eliminarToken(firebaseUidUsuario: String, token: String)

    /** Devuelve los tokens válidos de un usuario por su uid de Firebase. */
    fun obtenerTokensDe(firebaseUid: String): List<String>

    /** Borra el token cuando FCM lo declara inválido. */
    fun invalidarToken(token: String)
}
