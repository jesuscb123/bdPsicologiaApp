package dam2.tfg.psicologiaapp.backend.bdPsicologiaApp.web.dto.chatDTO

data class ChatResponse(
    val chatId: String,
    val interlocutorNombre: String,
    val interlocutorApellidos: String,
    val interlocutorFotoPerfilUrl: String?,
    val rtdbRuta: String
)
