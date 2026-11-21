package data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import data.entity.BookWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    //FILTER BOOKS
    @Transaction
    @Query(
        """
    SELECT * FROM books
    WHERE
        (
            :searchText = ''
            OR title LIKE '%' || :searchText || '%'
            OR author_name LIKE '%' || :searchText || '%'
        )
    AND
        (
            :level IS NULL
            OR english_level = :level
        )
    AND
        (
            :genreName IS NULL
            OR book_id IN (
                SELECT book_genres.book_id
                FROM book_genres
                INNER JOIN genres ON genres.genre_id = book_genres.genre_id
                WHERE genres.genre_name = :genreName
            )
        )
    ORDER BY title
    """
    )
    fun searchBooks(
        searchText: String,
        level: String?,
        genreName: String?
    ): Flow<List<BookWithGenres>>

    //GET ALL GENRES
    @Query("SELECT genre_name FROM genres ORDER BY genre_name")
    fun getGenreNames(): Flow<List<String>>

}

