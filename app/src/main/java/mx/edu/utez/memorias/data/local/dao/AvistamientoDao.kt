package mx.edu.utez.memorias.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.memorias.data.local.entity.AvistamientoEntity

@Dao
interface AvistamientoDao {
    @Query("SELECT * FROM avistamientos ORDER BY timestamp DESC")
    fun observeAllAvistamientos(): Flow<List<AvistamientoEntity>>

    @Query("SELECT * FROM avistamientos WHERE is_synced = False ORDER BY timestamp DESC")
    suspend fun getUnsyncedAvistamientos(): List<AvistamientoEntity>

    @Query("SELECT * FROM avistamientos WHERE user_matricula = :matricula ORDER BY timestamp DESC")
    fun observeAvistamientosByUser(matricula: String): Flow<List<AvistamientoEntity>>

    @Query("SELECT * FROM avistamientos WHERE id = :avistamientoId LIMIT 1")
    suspend fun getAvistamientoById(avistamientoId: Int): AvistamientoEntity?

    @Query("SELECT * FROM avistamientos WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getAvistamientoByRemoteId(remoteId: Int): AvistamientoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvistamiento(avistamiento: AvistamientoEntity): Long

    // TODO: Escribe la consulta (updateAvistamiento) Room SQL para oactualizar una entidad de avistamiento

    @Query("UPDATE avistamientos SET is_deleted = 1, is_synced = :isSynced WHERE id = :avistamientoId")
    suspend fun logicalDeleteAvistamiento(avistamientoId: Int, isSynced: Boolean)

    // TODO: Escribe la consulta (observeAvistamientoById) Room SQL para obtener un avistamiento por id pasado como parametro


}
