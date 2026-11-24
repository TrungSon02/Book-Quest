package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import data.dao.BookDao
import data.dao.PreferenceDao
import data.entity.BookEntity
import data.entity.BookFileEntity
import data.entity.BookGenreCrossRef
import data.entity.ChapterEntity
import data.entity.GenreEntity
import data.entity.UserEntity
import data.entity.UserGenreCrossRef

//The bundled bookquest.db is version 1 and has no user tables,
//so version 2 adds them without touching the asset file
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                user_id               INTEGER PRIMARY KEY AUTOINCREMENT,
                username              TEXT NOT NULL,
                email                 TEXT NOT NULL,
                password_hash         TEXT NOT NULL,
                reading_goal          TEXT CHECK (reading_goal IN ('HABIT_FORMATION', 'ENGLISH_PRACTICE')),
                english_level         TEXT CHECK (english_level IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')),
                preferences_completed INTEGER NOT NULL DEFAULT 0,
                created_at            TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """
        )
        db.execSQL("CREATE UNIQUE INDEX index_users_email ON users (email)")
        db.execSQL(
            """
            CREATE TABLE user_genres (
                user_id  INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                genre_id INTEGER NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,
                PRIMARY KEY (user_id, genre_id)
            )
            """
        )
        db.execSQL("CREATE INDEX index_user_genres_genre_id ON user_genres (genre_id)")
    }
}

@Database(
    entities = [
        GenreEntity::class,
        BookEntity::class,
        BookGenreCrossRef::class,
        ChapterEntity::class,
        BookFileEntity::class,
        UserEntity::class,
        UserGenreCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bookquest.db"
            )
                .createFromAsset("database/bookquest.db")
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}