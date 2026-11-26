package data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_library",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "book_id"], unique = true),
        Index(value = ["book_id"])
    ]
)
data class UserLibraryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_library_id") val id: Long? = null,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    val status: String = "WANT_TO_READ",
    @ColumnInfo(name = "is_favorite", defaultValue = "0") val isFavorite: Boolean = false,
    @ColumnInfo(name = "added_at", defaultValue = "CURRENT_TIMESTAMP") val addedAt: String = ""
)

@Entity(
    tableName = "reading_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "book_id"]),
        Index(value = ["book_id"])
    ]
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "session_id") val id: Long? = null,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "started_at") val startedAt: String,
    @ColumnInfo(name = "ended_at") val endedAt: String? = null,
    @ColumnInfo(name = "duration_seconds", defaultValue = "0") val durationSeconds: Int = 0,
    @ColumnInfo(name = "words_read", defaultValue = "0") val wordsRead: Int = 0,
    @ColumnInfo(name = "start_position") val startPosition: String,
    @ColumnInfo(name = "end_position") val endPosition: String? = null
)

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChapterEntity::class,
            parentColumns = ["chapter_id"],
            childColumns = ["chapter_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["user_id", "book_id"], unique = true),
        Index(value = ["book_id"]),
        Index(value = ["chapter_id"])
    ]
)
data class ReadingProgressEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "progress_id") val id: Long? = null,
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "chapter_id") val chapterId: Long? = null,
    @ColumnInfo(name = "current_position") val currentPosition: String,
    @ColumnInfo(name = "progress_percent", defaultValue = "0.0") val progressPercent: Double = 0.0,
    @ColumnInfo(name = "last_read_at", defaultValue = "CURRENT_TIMESTAMP") val lastReadAt: String = ""
)
