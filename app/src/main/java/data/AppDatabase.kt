package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import data.dao.BookDao
import data.entity.BookEntity
import data.entity.BookFileEntity
import data.entity.BookGenreCrossRef
import data.entity.ChapterEntity
import data.entity.GenreEntity

@Database(
    entities = [
        GenreEntity::class,
        BookEntity::class,
        BookGenreCrossRef::class,
        ChapterEntity::class,
        BookFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bookquest.db"
            )
                .createFromAsset("database/bookquest.db")
                .build()
                .also { instance = it }
        }
    }
}