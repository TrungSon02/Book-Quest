package data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val id: Long? = null,
    val username: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "english_level") val englishLevel: String,
    val timezone: String = "UTC",
    @ColumnInfo(name = "daily_reading_goal") val dailyReadingGoal: Int = 15,
    @ColumnInfo(name = "account_role") val accountRole: String = "learner",
    @ColumnInfo(name = "coin_balance", defaultValue = "0") val coinBalance: Int = 0,
    @ColumnInfo(name = "current_reading_streak", defaultValue = "0") val currentReadingStreak: Int = 0,
    @ColumnInfo(name = "last_day_streak_claimed") val lastDayStreakClaimed: String? = null,
    @ColumnInfo(name = "habit_21d_enabled", defaultValue = "0") val habit21dEnabled: Boolean = false,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: String = ""
)

@Entity(
    tableName = "user_genres",
    primaryKeys = ["user_id", "genre_id"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["genre_id"])]
)
data class UserGenreCrossRef(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "genre_id") val genreId: Long
)

@Entity(
    tableName = "user_books",
    primaryKeys = ["user_id", "book_id"],
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
    indices = [Index(value = ["book_id"])]
)
data class UserBookCrossRef(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "book_id") val bookId: Long
)
