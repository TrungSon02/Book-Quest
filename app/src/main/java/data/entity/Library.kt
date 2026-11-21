package data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "genre_id") val id: Long,
    @ColumnInfo(name = "genre_name") val name: String
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "book_id") val id: Long,
    @ColumnInfo(name = "author_name") val author: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "english_level") val level: String,
    @ColumnInfo(name = "word_count") val wordCount: Int,
    @ColumnInfo(name = "cover_image_url") val coverPath: String,
    @ColumnInfo(name = "publication_date") val publicationDate: String,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: String
)

@Entity(
    tableName = "book_genres",
    primaryKeys = ["book_id", "genre_id"],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

data class BookGenreCrossRef(
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "genre_id") val genreId: Long
)

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "chapter_id") val id: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "chapter_number") val number: Int,
    val title: String,
    @ColumnInfo(name = "word_count") val wordCount: Int,
    @ColumnInfo(name = "epub_chapter_ref") val epubRef: String
)

@Entity(
    tableName = "book_files",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["book_id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BookFileEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "file_id") val id: Long,
    @ColumnInfo(name = "book_id") val bookId: Long,
    val format: String,
    @ColumnInfo(name = "file_url") val fileUrl: String,
    @ColumnInfo(name = "file_size") val fileSize: Long,
    @ColumnInfo(name = "uploaded_at", defaultValue = "CURRENT_TIMESTAMP") val uploadedAt: String
)