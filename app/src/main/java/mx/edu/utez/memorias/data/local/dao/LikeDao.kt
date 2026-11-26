package mx.edu.utez.memorias.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.memorias.data.local.entity.LikeEntity

@Dao
interface LikeDao {
    @Query("SELECT COUNT(*) FROM likes WHERE avistamiento_id = :avistamientoId")
    fun observeLikeCount(avistamientoId: Int): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE avistamiento_id = :avistamientoId AND liker_matricula = :matricula)")
    fun observeIsLikedByUser(avistamientoId: Int, matricula: String): Flow<Boolean>

    @Query("SELECT * FROM likes WHERE avistamiento_id = :avistamientoId AND liker_matricula = :matricula LIMIT 1")
    suspend fun getLike(avistamientoId: Int, matricula: String): LikeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLike(like: LikeEntity): Long

    @Query("DELETE FROM likes WHERE avistamiento_id = :avistamientoId AND liker_matricula = :matricula")
    suspend fun deleteLike(avistamientoId: Int, matricula: String)
}
