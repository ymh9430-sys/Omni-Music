package com.example.materialyouplayer.data.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import androidx.room.Entity
import androidx.room.Index

// الكائن المتكامل للأغنية: بيجيب الأغنية وبيفصص كل فنانينها وأنواعها تلقائيًا
data class SongWithDetails(
    @Embedded val song: SongEntity,
    
    // جلب جميع الفنانين المرتبطين بهذه الأغنية عبر جدول الربط
    @Relation(
        parentColumn = "songId",
        entityColumn = "artistId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val artists: List<ArtistEntity>,

    // جلب جميع الأنواع الموسيقية المرتبطة بهذه الأغنية عبر جدول الربط
    @Relation(
        parentColumn = "songId",
        entityColumn = "genreId",
        associateBy = Junction(SongGenreCrossRef::class)
    )
    val genres: List<GenreEntity>
)

// كائن الفنان: بيجيب الفنان وكل الأغاني اللي شارك فيها
data class ArtistWithSongs(
    @Embedded val artist: ArtistEntity,
    @Relation(
        parentColumn = "artistId",
        entityColumn = "songId",
        associateBy = Junction(SongArtistCrossRef::class)
    )
    val songs: List<SongEntity>
)

// كائن النوع الموسيقي: بيجيب الـ Genre وكل الأغاني التابعة ليه
data class GenreWithSongs(
    @Embedded val genre: GenreEntity,
    @Relation(
        parentColumn = "genreId",
        entityColumn = "songId",
        associateBy = Junction(SongGenreCrossRef::class)
    )
    val songs: List<SongEntity>
)

// كائن الألبوم: بيجيب الألبوم وكل الأغاني اللي جواه
data class AlbumWithSongs(
    @Embedded val album: AlbumEntity,
    @Relation(
        parentColumn = "albumId",
        entityColumn = "songId"
    )
    val songs: List<SongEntity>
)

// جداول الربط الفرعية مع إضافة الـ Indices لمنع تحذيرات Room وتسريع الـ Queries
@Entity(
    primaryKeys = ["songId", "artistId"],
    indices = [Index(value = ["artistId"])]
)
data class SongArtistCrossRef(
    val songId: Long,
    val artistId: Long
)

@Entity(
    primaryKeys = ["songId", "genreId"],
    indices = [Index(value = ["genreId"])]
)
data class SongGenreCrossRef(
    val songId: Long,
    val genreId: Long
)
