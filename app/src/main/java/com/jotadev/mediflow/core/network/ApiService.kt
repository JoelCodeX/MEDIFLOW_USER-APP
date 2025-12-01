package com.jotadev.mediflow.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT

data class FirebaseTokenRequest(val id_token: String)

data class SyncFirebaseUserRequest(
    val uid_firebase: String,
    val nombre: String? = null,
    val apellido: String? = null,
    val rol: String? = null,
    val dni: String? = null,
    val area: String? = null,
    val cargo: String? = null
)

data class HorarioDto(
    val id: Int,
    val dia_semana: String,
    val hora_entrada: String,
    val hora_salida: String,
    val hora_refrigerio: String?,
    val duracion_refrigerio: Int,
    val turno: String,
    val vigente: Boolean,
    val fecha_creacion: String?
)

data class UsuarioDto(
    val id: Int,
    val uid_firebase: String?,
    val nombre: String,
    val apellido: String,
    val correo: String
)

data class MarcarEntradaRequest(
    val id_usuario: Int,
    val fecha: String? = null,
    val ubicacion_marcado: String? = null,
    val dispositivo_marcado: String? = null,
    val observacion: String? = null
)

data class RegistrarSalidaRequest(
    val id_usuario: Int
)

data class AsistenciaActualDto(
    val estado: String?,
    val hora_entrada: String?,
    val hora_salida: String?
)

// ===== Models para Encuestas =====
data class EncuestaDto(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val tipo: String? = null,
    val preguntas: List<PreguntaDto>? = null,
    val respondida_hoy: Boolean? = null
)

data class PreguntaDto(
    val id: Int,
    val tipo: String, // "texto" | "multiple" | "likert"
    val texto: String,
    val opciones: List<String>? = null // para multiple/likert
)

data class RespuestaItem(
    val pregunta_id: Int,
    val respuesta: String
)

data class ResponderEncuestaRequest(
    val id_usuario: Int,
    val respuestas: List<RespuestaItem>
)

data class IndicadoresDto(
    val bienestar_promedio_pct: Float,
    val salud_emocional_prom7_pct: Float,
    val asistencia_semanal_pct: Float,
    val motivacion_general_pct: Float
)

interface ApiService {
    @POST("/api/auth/firebase")
    suspend fun syncFirebase(@Body body: FirebaseTokenRequest): Response<Unit>

    @POST("/api/usuarios/sync-firebase")
    suspend fun syncFirebaseUser(@Body body: SyncFirebaseUserRequest): Response<Unit>

    @GET("/api/horarios/by-uid/{uid}")
    suspend fun getHorariosByUid(
        @Path("uid") uid: String,
        @Query("vigente") vigente: Boolean = true
    ): Response<List<HorarioDto>>

    @GET("/api/usuarios/by-uid/{uid}")
    suspend fun getUsuarioByUid(
        @Path("uid") uid: String
    ): Response<UsuarioDto>

    @POST("/api/asistencia/marcar")
    suspend fun marcarEntrada(@Body body: MarcarEntradaRequest): Response<Map<String, Any>>

    @PUT("/api/asistencia/salida")
    suspend fun registrarSalida(@Body body: RegistrarSalidaRequest): Response<Map<String, Any>>

    @GET("/api/asistencia/actual")
    suspend fun getAsistenciaActual(
        @Query("id_usuario") idUsuario: Int
    ): Response<AsistenciaActualDto>

    // Encuestas: pendientes para el usuario
    @GET("/api/encuestas/pendientes/{id_usuario}")
    suspend fun getEncuestasPendientes(
        @Path("id_usuario") idUsuario: Int
    ): Response<List<EncuestaDto>>

    // Encuesta por ID (incluye preguntas)
    @GET("/api/encuestas/{id}")
    suspend fun getEncuesta(
        @Path("id") id: Int
    ): Response<EncuestaDto>

    // Responder encuesta
    @POST("/api/encuestas/{id}/responder")
    suspend fun responderEncuesta(
        @Path("id") id: Int,
        @Body body: ResponderEncuestaRequest
    ): Response<Map<String, Any>>

    // Indicadores para el Home
    @GET("/api/encuestas/indicadores/{id_usuario}")
    suspend fun getIndicadores(
        @Path("id_usuario") idUsuario: Int
    ): Response<IndicadoresDto>
}