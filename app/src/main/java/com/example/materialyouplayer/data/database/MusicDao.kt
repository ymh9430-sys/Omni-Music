package com.example.materialyouplayer.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // ==========================================
    // عمليات الجلب والقراءة (تحديث تلقائي للـ UI عبر Flow)
    // ==========================================

    @Transaction
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongWithDetails>>

    @Transaction
    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getRecentlyAddedSongs(): Flow<List<SongWithDetails>>

    @Transaction
    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumWithSongs>>

    @Transaction
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistWithSongs>>

    @Transaction
    @Query("SELECT * FROM genres ORDER BY name ASC")
    fun getAllGenres(): Flow<List<GenreWithSongs>>

    @Transaction
    @Query("SELECT * FROM songs WHERE songId = :songId")
    suspend fun getSongById(songId: String): SongWithDetails?

    // ==========================================
    // عمليات الإدخال والحفظ (Insert Operations)
    // ==========================================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: GenreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongArtistCrossRef(crossRef: SongArtistCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongGenreCrossRef(crossRef: SongGenreCrossRef)

    // ==========================================
    // معالجة تحديثات الـ Tag Editor المتقدم والـ Lyrics
    // ==========================================

    @Query("UPDATE songs SET lyrics = :newLyrics WHERE songId = :songId")
    suspend fun updateLyrics(songId: String, newLyrics: String?)

    @Update
    suspend fun updateSongEntity(song: SongEntity)

    // مسح علاقات الفنانين والأنواع القديمة لأغنية معينة تمهيداً لتحديثها بالـ Tags الجديدة
    @Query("DELETE FROM song_artist_cross_ref WHERE songId = :songId")
    suspend fun deleteArtistCrossRefsForSong(songId: String)

    @Query("DELETE FROM song_genre_cross_ref WHERE songId = :songId")
    suspend fun deleteGenreCrossRefsForSong(songId: String)

    // دالة المعاملات المركبة (Transaction) لتحديث الـ Tags بالكامل بشكل آمن وصحيح
    @Transaction
    suspend fun updateSongTags(
        song: SongEntity,
        artists: List<ArtistEntity>,
        genres: List<GenreEntity>
    ) {
        // 1. تحديث بيانات الأغنية الأساسية (العنوان، السنة، الملحن...)
        updateSongEntity(song)

        // 2. مسح الروابط القديمة للفنانين والأنواع لمنع التكرار العشوائي
        deleteArtistCrossRefsForSong(song.songId)
        deleteGenreCrossRefsForSong(song.songId)

        // 3. إدخال الفنانين والروابط الجديدة
        artists.forEach { artist ->
            insertArtist(artist)
            insertSongArtistCrossRef(SongArtistCrossRef(song.songId, artist.artistId))
        }

        // 4. إدخال الأنواع والروابط الجديدة
        genres.forEach { genre ->
            insertGenre(genre)
            insertSongGenreCrossRef(SongGenreCrossRef(song.songId, genre.genreId))
        }
    }
}
