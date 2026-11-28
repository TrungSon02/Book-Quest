package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.dao.BookDao
import data.dao.ReadingDao
import data.dao.UserDao
import data.entity.BookEntity
import data.entity.BookFileEntity
import data.entity.BookGenreCrossRef
import data.entity.ChapterEntity
import data.entity.GenreEntity
import data.entity.ReadingProgressEntity
import data.entity.ReadingSessionEntity
import data.entity.UserBookCrossRef
import data.entity.UserEntity
import data.entity.UserGenreCrossRef
import data.entity.UserLibraryEntity

@Database(
    entities = [
        GenreEntity::class,
        BookEntity::class,
        BookGenreCrossRef::class,
        ChapterEntity::class,
        BookFileEntity::class,
        UserEntity::class,
        UserGenreCrossRef::class,
        UserBookCrossRef::class,
        UserLibraryEntity::class,
        ReadingSessionEntity::class,
        ReadingProgressEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bookquest.db"
            )
                .createFromAsset("database/bookquest.db")
                .fallbackToDestructiveMigration(true)
                .build()
                .also { instance = it }
        }
    }
}
