package data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//Options for the preference page (names match books.english_level for the levels)
enum class ReadingGoal(val label: String, val description: String) {
    HABIT_FORMATION("Build a reading habit", "Read a little every day and stay consistent"),
    ENGLISH_PRACTICE("Practice English reading", "Improve vocabulary and comprehension")
}

enum class EnglishLevel(val label: String) {
    A1("A1 · Beginner"),
    A2("A2 · Elementary"),
    B1("B1 · Intermediate"),
    B2("B2 · Upper-intermediate"),
    C1("C1 · Advanced"),
    C2("C2 · Proficient")
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "user_id") val id: Long?,
    val username: String,
    val email: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "reading_goal") val readingGoal: String? = null,
    @ColumnInfo(name = "english_level") val englishLevel: String? = null,
    @ColumnInfo(name = "preferences_completed", defaultValue = "0") val preferencesCompleted: Boolean = false,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: String
)

//Link table: which genres a user likes (many-to-many, same idea as book_genres)
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
    indices = [Index("genre_id")]
)
data class UserGenreCrossRef(
    @ColumnInfo(name = "user_id") val userId: Long,
    @ColumnInfo(name = "genre_id") val genreId: Long
)