package mx.edu.utez.memorias.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PendingNotificationDto(
    val id: Int,
    @SerializedName("avistamiento_id") val avistamientoId: Int,
    @SerializedName("avistamiento_titulo") val avistamientoTitulo: String,
    @SerializedName("liker_matricula") val likerMatricula: String,
    @SerializedName("liker_name") val likerName: String,
    @SerializedName("created_at") val createdAt: String
)
