package mx.edu.utez.memorias.data.remote

import mx.edu.utez.memorias.data.remote.dto.AuthResponse
import mx.edu.utez.memorias.data.remote.dto.AvistamientoDto
import mx.edu.utez.memorias.data.remote.dto.EditHistoryDto
import mx.edu.utez.memorias.data.remote.dto.LikeToggleRequest
import mx.edu.utez.memorias.data.remote.dto.LikeToggleResponse
import mx.edu.utez.memorias.data.remote.dto.LoginRequest
import mx.edu.utez.memorias.data.remote.dto.PendingNotificationDto
import mx.edu.utez.memorias.data.remote.dto.RegisterRequest
import mx.edu.utez.memorias.data.remote.dto.UpdateAvistamientoRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/avistamientos")
    suspend fun getAvistamientos(): Response<List<AvistamientoDto>>

    // TODO: Agrega la etiqueta para enviar datos del formulario por partes
    @POST("api/avistamientos")
    suspend fun createAvistamiento(
        // TODO: Agrega Todas las partes del formulario de avistamientos:
        // user_matricula, titulo, lugar, latitud, longitud, descripcion, forma_ovni(opcional) y timestamp
        // todos los datos son de tipo RequestBody
        @Part image: MultipartBody.Part?
    ): Response<AvistamientoDto>

    @PUT("api/avistamientos/{id}")
    suspend fun updateAvistamiento(
        @Path("id") avistamientoId: Int,
        @Query("user_matricula") userMatricula: String,
        @Body request: UpdateAvistamientoRequest
    ): Response<AvistamientoDto>

    @DELETE("api/avistamientos/{id}")
    suspend fun deleteAvistamiento(
        @Path("id") avistamientoId: Int,
        @Query("user_matricula") userMatricula: String
    ): Response<Unit>

    @POST("api/avistamientos/{id}/like")
    suspend fun toggleLike(
        @Path("id") avistamientoId: Int,
        @Body request: LikeToggleRequest
    ): Response<LikeToggleResponse>

    @GET("api/avistamientos/{id}/history")
    suspend fun getEditHistory(@Path("id") avistamientoId: Int): Response<List<EditHistoryDto>>

    @GET("api/notifications/pending/{matricula}")
    suspend fun getPendingNotifications(
        @Path("matricula") matricula: String
    ): Response<List<PendingNotificationDto>>
}
