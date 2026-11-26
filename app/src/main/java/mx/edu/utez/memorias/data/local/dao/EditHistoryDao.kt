package mx.edu.utez.memorias.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.memorias.data.local.entity.EditHistoryEntity

@Dao
interface EditHistoryDao {
    @Query("SELECT * FROM edit_history WHERE avistamiento_id = :avistamientoId ORDER BY edited_at DESC")
    fun observeHistoryForAvistamiento(avistamientoId: Int): Flow<List<EditHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: EditHistoryEntity)
}
