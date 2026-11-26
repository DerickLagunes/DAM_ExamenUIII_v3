package mx.edu.utez.memorias.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.edu.utez.memorias.data.local.dao.AvistamientoDao
import mx.edu.utez.memorias.data.local.dao.EditHistoryDao
import mx.edu.utez.memorias.data.local.dao.LikeDao
import mx.edu.utez.memorias.data.local.dao.UserDao
import mx.edu.utez.memorias.data.local.entity.AvistamientoEntity
import mx.edu.utez.memorias.data.local.entity.EditHistoryEntity
import mx.edu.utez.memorias.data.local.entity.LikeEntity
import mx.edu.utez.memorias.data.local.entity.UserEntity
import mx.edu.utez.memorias.data.remote.ApiService
import mx.edu.utez.memorias.data.remote.dto.LikeToggleRequest
import mx.edu.utez.memorias.data.remote.dto.UpdateAvistamientoRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.Instant

data class AvistamientoUiModel(
    val entity: AvistamientoEntity,
    val likesCount: Int,
    val isLikedByCurrentUser: Boolean
)

class AvistamientoRepository(
    private val apiService: ApiService,
    private val avistamientoDao: AvistamientoDao,
    private val userDao: UserDao,
    private val likeDao: LikeDao,
    private val editHistoryDao: EditHistoryDao
) {
    fun observeAllAvistamientosWithLikes(currentMatricula: String): Flow<List<AvistamientoUiModel>> {
        return avistamientoDao.observeAllAvistamientos().combine(
            kotlinx.coroutines.flow.flowOf(currentMatricula)
        ) { avistamientos, matricula ->
            avistamientos.map { avistamiento ->
                AvistamientoUiModel(
                    entity = avistamiento,
                    likesCount = 0,
                    isLikedByCurrentUser = false
                )
            }
        }
    }

    fun observeAllAvistamientos(): Flow<List<AvistamientoEntity>> = avistamientoDao.observeAllAvistamientos()

    fun observeUserAvistamientos(matricula: String): Flow<List<AvistamientoEntity>> =
        avistamientoDao.observeAvistamientosByUser(matricula)

    fun observeAvistamientoById(avistamientoId: Int): Flow<AvistamientoEntity?> =
        avistamientoDao.observeAvistamientoById(avistamientoId)

    fun observeLikeCount(avistamientoId: Int): Flow<Int> =
        likeDao.observeLikeCount(avistamientoId)

    fun observeIsLikedByUser(avistamientoId: Int, matricula: String): Flow<Boolean> =
        likeDao.observeIsLikedByUser(avistamientoId, matricula)

    fun observeEditHistory(avistamientoId: Int): Flow<List<EditHistoryEntity>> =
        editHistoryDao.observeHistoryForAvistamiento(avistamientoId)

    suspend fun getAvistamientoById(avistamientoId: Int): AvistamientoEntity? =
        avistamientoDao.getAvistamientoById(avistamientoId)

    suspend fun refreshAvistamientosFromRemote(): Result<Unit> {
        return try {
            val response = apiService.getAvistamientos()
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                for (dto in remoteList) {
                    userDao.insertUserIgnore(
                        UserEntity(
                            matricula = dto.userMatricula,
                            nombres = dto.authorName ?: dto.userMatricula,
                            apellidos = "",
                            passwordHash = "",
                            createdAt = dto.timestamp
                        )
                    )
                    val existingLocal = avistamientoDao.getAvistamientoByRemoteId(dto.id)
                        ?: avistamientoDao.getAvistamientoById(dto.id)

                    val entity = AvistamientoEntity(
                        id = existingLocal?.id ?: dto.id,
                        userMatricula = dto.userMatricula,
                        titulo = dto.titulo,
                        lugar = dto.lugar,
                        latitud = dto.latitud,
                        longitud = dto.longitud,
                        descripcion = dto.descripcion,
                        formaOvni = dto.formaOvni,
                        imageFilename = dto.imageFilename,
                        timestamp = dto.timestamp,
                        isDeleted = dto.isDeleted,
                        isSynced = true,
                        localImagePath = existingLocal?.localImagePath,
                        remoteId = dto.id
                    )
                    avistamientoDao.insertAvistamiento(entity)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAvistamiento(
        userMatricula: String,
        titulo: String,
        lugar: String,
        latitud: Double,
        longitud: Double,
        descripcion: String,
        formaOvni: String?,
        localImagePath: String?,
        captureTimestamp: String
    ): Result<Boolean> {
        // TODO: Inserta en la base de datos local la Entidad de avistamiento registrada y recupera su "localID" para buscarlo posteriormente en linea val savedEntity =...

        val savedEntity = avistamientoDao.getAvistamientoById(localId) ?: return Result.success(false)
        return uploadSingleAvistamiento(savedEntity)
    }

    suspend fun uploadSingleAvistamiento(avistamiento: AvistamientoEntity): Result<Boolean> {
        return try {
            val textPlain = "text/plain".toMediaTypeOrNull()
            val imagePart = avistamiento.localImagePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    val reqFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, reqFile)
                } else null
            }

            val response = apiService.createAvistamiento(
                userMatricula = avistamiento.userMatricula.toRequestBody(textPlain),
                titulo = avistamiento.titulo.toRequestBody(textPlain),
                lugar = avistamiento.lugar.toRequestBody(textPlain),
                latitud = avistamiento.latitud.toString().toRequestBody(textPlain),
                longitud = avistamiento.longitud.toString().toRequestBody(textPlain),
                descripcion = avistamiento.descripcion.toRequestBody(textPlain),
                formaOvni = avistamiento.formaOvni?.toRequestBody(textPlain),
                timestamp = avistamiento.timestamp.toRequestBody(textPlain),
                image = imagePart
            )

            if (response.isSuccessful && response.body() != null) {
                val remoteDto = response.body()!!
                val updatedEntity = avistamiento.copy(
                    imageFilename = remoteDto.imageFilename,
                    isSynced = true,
                    remoteId = remoteDto.id
                )
                avistamientoDao.updateAvistamiento(updatedEntity)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    suspend fun syncPendingOfflineAvistamientos(): Int {
        val unsynced = avistamientoDao.getUnsyncedAvistamientos()
        var syncedCount = 0
        for (avistamiento in unsynced) {
            val result = uploadSingleAvistamiento(avistamiento)
            if (result.getOrDefault(false)) {
                syncedCount++
            }
        }
        return syncedCount
    }

    suspend fun updateAvistamiento(
        avistamientoId: Int,
        userMatricula: String? = null,
        titulo: String,
        lugar: String,
        latitud: Double,
        longitud: Double,
        descripcion: String,
        formaOvni: String? = null
    ): Result<Unit> {
        val current = avistamientoDao.getAvistamientoById(avistamientoId)
            ?: return Result.failure(Exception("Avistamiento no encontrado"))

        val matricula = userMatricula ?: current.userMatricula

        val changes = buildList {
            if (current.titulo != titulo) add("Título actualizado")
            if (current.lugar != lugar) add("Lugar actualizado a $lugar")
            if (current.latitud != latitud || current.longitud != longitud) add("Ubicación ajustada en mapa")
            if (current.descripcion != descripcion) add("Descripción modificada")
            if (current.formaOvni != formaOvni) add("Forma del Ovni modificada")
        }.ifEmpty { listOf("Edición general del avistamiento") }.joinToString(" • ")

        val editedAt = Instant.now().toString()

        avistamientoDao.updateAvistamiento(
            current.copy(
                titulo = titulo,
                lugar = lugar,
                latitud = latitud,
                longitud = longitud,
                descripcion = descripcion,
                formaOvni = formaOvni,
                isSynced = false
            )
        )
        editHistoryDao.insertHistory(
            EditHistoryEntity(
                avistamientoId = avistamientoId,
                editedAt = editedAt,
                changeSummary = changes
            )
        )

        val targetRemoteId = current.remoteId ?: current.id
        try {
            val resp = apiService.updateAvistamiento(
                targetRemoteId,
                matricula,
                UpdateAvistamientoRequest(
                    userMatricula = matricula,
                    titulo = titulo,
                    lugar = lugar,
                    latitud = latitud,
                    longitud = longitud,
                    descripcion = descripcion,
                    formaOvni = formaOvni,
                    changeSummary = changes
                )
            )
            if (resp.isSuccessful) {
                avistamientoDao.updateAvistamiento(
                    current.copy(
                        titulo = titulo,
                        lugar = lugar,
                        latitud = latitud,
                        longitud = longitud,
                        descripcion = descripcion,
                        formaOvni = formaOvni,
                        isSynced = true
                    )
                )
            }
        } catch (_: Exception) {
        }
        return Result.success(Unit)
    }

    suspend fun toggleLike(avistamientoId: Int, likerMatricula: String) {
        val existing = likeDao.getLike(avistamientoId, likerMatricula)
        if (existing != null) {
            likeDao.deleteLike(avistamientoId, likerMatricula)
        } else {
            likeDao.insertLike(
                LikeEntity(
                    avistamientoId = avistamientoId,
                    likerMatricula = likerMatricula,
                    createdAt = Instant.now().toString(),
                    notified = 0
                )
            )
        }

        val avistamiento = avistamientoDao.getAvistamientoById(avistamientoId)
        val remoteId = avistamiento?.remoteId ?: avistamientoId
        try {
            apiService.toggleLike(remoteId, LikeToggleRequest(likerMatricula))
        } catch (_: Exception) {
        }
    }

    suspend fun logicalDeleteAvistamiento(avistamientoId: Int, currentUserMatricula: String): Result<Unit> {
        val avistamiento = avistamientoDao.getAvistamientoById(avistamientoId)
            ?: return Result.failure(Exception("Avistamiento no encontrado"))

        if (avistamiento.userMatricula != currentUserMatricula) {
            return Result.failure(Exception("Solo el autor puede eliminar este avistamiento"))
        }

        avistamientoDao.logicalDeleteAvistamiento(avistamientoId, isSynced = false)

        val remoteId = avistamiento.remoteId ?: avistamientoId
        try {
            val resp = apiService.deleteAvistamiento(remoteId, currentUserMatricula)
            if (resp.isSuccessful) {
                avistamientoDao.logicalDeleteAvistamiento(avistamientoId, isSynced = true)
            }
        } catch (_: Exception) {
        }
        return Result.success(Unit)
    }

    suspend fun refreshEditHistory(avistamientoId: Int) {
        val avistamiento = avistamientoDao.getAvistamientoById(avistamientoId) ?: return
        val remoteId = avistamiento.remoteId ?: avistamientoId
        try {
            val resp = apiService.getEditHistory(remoteId)
            if (resp.isSuccessful && resp.body() != null) {
                resp.body()!!.forEach { dto ->
                    editHistoryDao.insertHistory(
                        EditHistoryEntity(
                            id = dto.id,
                            avistamientoId = avistamientoId,
                            editedAt = dto.editedAt,
                            changeSummary = dto.changeSummary
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
    }
}
