package mx.edu.utez.memorias.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "likes",
    foreignKeys = [
        ForeignKey(
            entity = AvistamientoEntity::class,
            parentColumns = ["id"],
            childColumns = ["avistamiento_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["matricula"],
            childColumns = ["liker_matricula"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["avistamiento_id"]),
        Index(value = ["liker_matricula"]),
        Index(value = ["avistamiento_id", "liker_matricula"], unique = true)
    ]
)
data class LikeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "avistamiento_id")
    val avistamientoId: Int,
    @ColumnInfo(name = "liker_matricula")
    val likerMatricula: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "notified")
    val notified: Int = 0
)
