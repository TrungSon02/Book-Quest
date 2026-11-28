package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import data.entity.UserBookCrossRef
import data.entity.UserEntity
import data.entity.UserGenreCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:username) LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(username) = LOWER(:identifier) OR LOWER(email) = LOWER(:identifier) LIMIT 1")
    suspend fun getUserByUsernameOrEmail(identifier: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(email) = LOWER(:email))")
    suspend fun isEmailTaken(email: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER(:username))")
    suspend fun isUsernameTaken(username: String): Boolean

    @Query("UPDATE users SET coin_balance = coin_balance + :delta WHERE user_id = :userId")
    suspend fun updateCoinBalance(userId: Long, delta: Int)

    @Query(
        """
        UPDATE users
        SET current_reading_streak = :streak,
            last_day_streak_claimed = :claimedDate
        WHERE user_id = :userId
        """
    )
    suspend fun updateReadingStreak(userId: Long, streak: Int, claimedDate: String?)

    @Query("UPDATE users SET habit_21d_enabled = :enabled WHERE user_id = :userId")
    suspend fun setHabit21dEnabled(userId: Long, enabled: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserGenres(refs: List<UserGenreCrossRef>)

    @Query("DELETE FROM user_genres WHERE user_id = :userId")
    suspend fun clearUserGenres(userId: Long)

    @Query("DELETE FROM user_genres WHERE user_id = :userId AND genre_id = :genreId")
    suspend fun deleteUserGenre(userId: Long, genreId: Long)

    @Query("SELECT genre_id FROM user_genres WHERE user_id = :userId")
    suspend fun getUserGenreIds(userId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserBooks(refs: List<UserBookCrossRef>)

    @Query("DELETE FROM user_books WHERE user_id = :userId AND book_id = :bookId")
    suspend fun deleteUserBook(userId: Long, bookId: Long)

    @Query("SELECT book_id FROM user_books WHERE user_id = :userId")
    suspend fun getUserBookIds(userId: Long): List<Long>
}
