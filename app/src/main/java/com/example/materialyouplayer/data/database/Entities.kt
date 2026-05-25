package com.example.materialyouplayer.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1. جدول الأغاني الأساسي
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val songId: String, // غالباً بيكون مسار الملف أو ID فريد
    val title: String,
    val albumId: String,
    val albumTitle: String,
    val duration: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val year: String,
    val composer: String,
    val lyrics: String? = null, // لدعم الـ Enhanced LRC Karaoke
    val path: String,
    val dateAdded: Long
)

// 2. جدول الفنانين
@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey val artistId: String,
    val name: String,
    val imageUrl: String? = null // لرفع الصور من الـ API لاحقاً
)

// 3. جدول الأنواع الموسيقية
@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey val genreId: String,
    val name: String
)

// 4. جدول الألبومات
@Entity(tableName = "albums")
data class AlbumEntity(
    @PrimaryKey val albumId: String,
    val title: String,
    val albumArtist: String,
    val coverPath: String?,
    val year: String
)

// ==========================================
// جداول الربط (Cross Reference) لـ Multiple Artists & Genres
// ==========================================

// ربط الأغاني بالفنانين (الأغنية ليها كذا فنان، والفنان ليه كذا أغنية)
@Entity(tableName = "song_artist_cross_ref", primaryKeys = ["songId", "artistId"])
data class SongArtistCrossRef(
    val songId: String,
    val artistId: String
)

// ربط الأغاني بالأنواع (الأغنية ليها كذا نوع، والنوع جواه كذا أغنية)
@Entity(tableName = "song_genre_cross_ref", primaryKeys = ["songId", "genreId"])
data class SongGenreCrossRef(
    val songId: String,
    val genreId: String
)
