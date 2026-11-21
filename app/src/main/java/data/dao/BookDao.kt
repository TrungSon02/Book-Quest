package data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import data.entity.BookWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Transaction
    @Query("SELECT * FROM books ORDER BY title")
    fun getAllBooks(): Flow<List<BookWithGenres>>
}