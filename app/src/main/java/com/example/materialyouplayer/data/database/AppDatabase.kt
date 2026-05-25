package com.example.materialyouplayer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SongEntity::class,
        ArtistEntity::class,
        GenreEntity::class,
        AlbumEntity::class,
        SongArtistCrossRef::class,
        SongGenreCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // دالة جلب قاعدة البيانات بشكل آمن لضمان عدم إنشاء أكثر من نسخة (Thread-Safe)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "material_you_player_db"
                )
                // استراتيجية مسح وإعادة بناء الجداول في حالة تغيير إصدار الـ DB بدون Migration حالياً
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
