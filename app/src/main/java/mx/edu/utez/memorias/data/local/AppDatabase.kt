package mx.edu.utez.memorias.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.utez.memorias.data.local.dao.AvistamientoDao
import mx.edu.utez.memorias.data.local.dao.EditHistoryDao
import mx.edu.utez.memorias.data.local.dao.LikeDao
import mx.edu.utez.memorias.data.local.dao.UserDao
import mx.edu.utez.memorias.data.local.entity.AvistamientoEntity
import mx.edu.utez.memorias.data.local.entity.EditHistoryEntity
import mx.edu.utez.memorias.data.local.entity.LikeEntity
import mx.edu.utez.memorias.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AvistamientoEntity::class,
        LikeEntity::class,
        EditHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun avistamientoDao(): AvistamientoDao
    abstract fun likeDao(): LikeDao
    abstract fun editHistoryDao(): EditHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "avistamientos_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
