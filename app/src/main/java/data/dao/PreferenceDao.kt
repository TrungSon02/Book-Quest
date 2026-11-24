package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import data.entity.GenreEntity
import data.entity.UserEntity
import data.entity.UserGenreCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    //GET ALL GENRES (with ids, BookDao.getGenreNames only gives the names)
    @Query("SELECT * FROM genres ORDER BY genre_name")
    fun getGenres(): Flow<List<GenreEntity>>

    //CHECK IF USER ALREADY FINISHED THE PREFERENCE PAGE
    @Query("SELECT preferences_completed FROM users WHERE user_id = :userId")
    suspend fun isPreferencesCompleted(userId: Long): Boolean?

    //SAVE PREFERENCES
    @Query(
        """
    UPDATE users
    SET reading_goal = :goal,
        english_level = :level,
        preferences_completed = 1
    WHERE user_id = :userId
    """
    )
    suspend fun updatePreferences(userId: Long, goal: String, level: String)

    @Query("DELETE FROM user_genres WHERE user_id = :userId")
    suspend fun clearUserGenres(userId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserGenres(refs: List<UserGenreCrossRef>)

    //Do the 3 steps together: if one fails, nothing is saved
    @Transaction
    suspend fun savePreferences(
        userId: Long,
        goal: String,
        level: String,
        genreIds: List<Long>
    ) {
        updatePreferences(userId, goal, level)
        clearUserGenres(userId)
        insertUserGenres(genreIds.map { genreId -> UserGenreCrossRef(userId, genreId) })
    }

    //TEMP: for testing only, the register page (2.2) should insert users instead
    @Insert
    suspend fun insertUser(user: UserEntity): Long
}