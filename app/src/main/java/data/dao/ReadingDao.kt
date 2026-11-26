package data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import data.entity.ReadingProgressEntity
import data.entity.ReadingSessionEntity
import data.entity.UserLibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLibraryEntry(entry: UserLibraryEntity): Long

    @Update
    suspend fun updateLibraryEntry(entry: UserLibraryEntity)

    @Query("SELECT * FROM user_library WHERE user_id = :userId AND book_id = :bookId LIMIT 1")
    suspend fun getLibraryEntry(userId: Long, bookId: Long): UserLibraryEntity?

    @Query("SELECT * FROM user_library WHERE user_id = :userId ORDER BY added_at DESC")
    fun observeUserLibrary(userId: Long): Flow<List<UserLibraryEntity>>

    @Query("SELECT * FROM user_library WHERE user_id = :userId AND is_favorite = 1 ORDER BY added_at DESC")
    fun observeFavoriteBooks(userId: Long): Flow<List<UserLibraryEntity>>

    @Transaction
    suspend fun upsertLibraryEntry(entry: UserLibraryEntity): Long {
        val existing = getLibraryEntry(entry.userId, entry.bookId)
        return if (existing == null) {
            insertLibraryEntry(entry)
        } else {
            updateLibraryEntry(entry.copy(id = existing.id))
            existing.id ?: 0L
        }
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReadingSession(session: ReadingSessionEntity): Long

    @Update
    suspend fun updateReadingSession(session: ReadingSessionEntity)

    @Query("SELECT * FROM reading_sessions WHERE user_id = :userId AND book_id = :bookId ORDER BY started_at DESC")
    suspend fun getSessionsForBook(userId: Long, bookId: Long): List<ReadingSessionEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReadingProgress(progress: ReadingProgressEntity): Long

    @Update
    suspend fun updateReadingProgress(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE user_id = :userId AND chapter_id = :chapterId LIMIT 1")
    suspend fun getReadingProgress(userId: Long, chapterId: Long): ReadingProgressEntity?

    @Query(
        """
        SELECT rp.* FROM reading_progress rp
        INNER JOIN chapters c ON c.chapter_id = rp.chapter_id
        WHERE rp.user_id = :userId AND c.book_id = :bookId
        ORDER BY rp.last_read_at DESC
        LIMIT 1
        """
    )
    suspend fun getReadingProgressForBook(userId: Long, bookId: Long): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE user_id = :userId ORDER BY last_read_at DESC")
    fun observeUserReadingProgress(userId: Long): Flow<List<ReadingProgressEntity>>

    @Transaction
    suspend fun upsertReadingProgress(progress: ReadingProgressEntity): Long {
        val existing = progress.chapterId?.let { getReadingProgress(progress.userId, it) }
        return if (existing == null) {
            insertReadingProgress(progress)
        } else {
            updateReadingProgress(progress.copy(id = existing.id))
            existing.id ?: 0L
        }
    }
}
