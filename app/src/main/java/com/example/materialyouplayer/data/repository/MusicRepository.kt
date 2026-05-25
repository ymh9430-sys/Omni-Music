package com.example.materialyouplayer.data.repository

import android.content.Context
import com.example.materialyouplayer.data.database.*
import com.example.materialyouplayer.data.scanner.MediaScanner
import kotlinx.coroutines.flow.Flow

class MusicRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val musicDao = database.musicDao()
    private val mediaScanner = MediaScanner(context)

    // ==========================================
    // دالة تشغيل الفحص والـ Scanning
    // ==========================================
    
    suspend fun refreshMediaLibrary() {
        mediaScanner.scanDeviceFiles()
        // هنا مستقبلاً هنضيف استدعاء الـ API لجلب صور الفنانين المفقودة تلقائيًا بعد الـ Scan
        fetchMissingArtistImages()
    }

    // ==========================================
    // دوال جلب البيانات للـ UI (تحديث حي عبر Flow)
    // ==========================================

    fun getAllSongs(): Flow<List<SongWithDetails>> = musicDao.getAllSongs()

    fun getRecentlyAdded(): Flow<List<SongWithDetails>> = musicDao.getRecentlyAddedSongs()

    fun getAllAlbums(): Flow<List<AlbumWithSongs>> = musicDao.getAllAlbums()

    fun getAllArtists(): Flow<List<ArtistWithSongs>> = musicDao.getAllArtists()

    fun getAllGenres(): Flow<List<GenreWithSongs>> = musicDao.getAllGenres()

    suspend fun getSongById(songId: String): SongWithDetails? = musicDao.getSongById(songId)

    // ==========================================
    // دوال الـ Tag Editor وتعديل الكلمات (Lyrics)
    // ==========================================

    // تحديث الكلمات المتزامنة (Karaoke LRC) من شاشة الـ Editor مباشرة
    suspend fun updateSongLyrics(songId: String, lyrics: String?) {
        musicDao.updateLyrics(songId, lyrics)
    }

    // تحديث الـ Tags المعقدة مع دعم الـ Multiple Artists & Genres بدون أخطاء تكرار
    suspend fun updateSongTags(
        songEntity: SongEntity,
        artistNames: List<String>,
        genreNames: List<String>
    ) {
        // تحويل الأسماء النصية إلى كائنات Entity جاهزة لقاعدة البيانات
        val artists = artistNames.map { name ->
            val artistId = name.lowercase().trim().replace(" ", "_")
            ArtistEntity(artistId = artistId, name = name.trim())
        }

        val genres = genreNames.map { name ->
            val genreId = name.lowercase().trim().replace(" ", "_")
            GenreEntity(genreId = genreId, name = name.trim())
        }

        // تنفيذ عملية التحديث المركبة داخل الـ Transaction الآمن في الـ DAO
        musicDao.updateSongTags(songEntity, artists, genres)
    }

    // ==========================================
    // جلب صور الفنانين من الـ API (Last.fm / iTunes)
    // ==========================================
    
    private suspend fun fetchMissingArtistImages() {
        // الكود الافتراضي لجلب الصور - سنقوم بربطه بـ Retrofit لاحقاً عند برمجة جزء الشبكة
    }
}
