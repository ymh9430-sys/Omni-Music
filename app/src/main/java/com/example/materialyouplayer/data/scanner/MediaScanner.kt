package com.example.materialyouplayer.data.scanner

import android.content.Context
import android.provider.MediaStore
import com.example.materialyouplayer.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context) {

    private val musicDao = AppDatabase.getDatabase(context).musicDao()

    // الدالة الأساسية لمسح الجهاز وجلب الأغاني
    suspend fun scanDeviceFiles() = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
        // تحديد البيانات المطلوبة من الـ MediaStore
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, // مسار الملف
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK
        )

        // فلترة الملفات علشان نجيب الأغاني فقط ونستبعد الفويس نوتس والـ Ringtones
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown Title"
                val rawArtist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val albumTitle = cursor.getString(albumColumn) ?: "Unknown Album"
                val albumId = cursor.getString(albumIdColumn) ?: "unknown_album"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val year = cursor.getString(yearColumn) ?: "Unknown Year"
                val trackNumber = cursor.getInt(trackColumn)

                // تخطي الملف لو ممسوح أو مش موجود فعلياً
                if (path == null || !File(path).exists()) continue

                // 1. تفكيك وفصل الـ Multiple Artists بناءً على الفواصل الشائعة في الـ Tags
                val parsedArtists = parseMultipleItems(rawArtist)

                // 2. جلب الـ Genre (MediaStore لا يوفره مباشرة في الـ Media core، سنقوم بعمل حقل افتراضي ويتم تحديثه عبر الـ Tag Editor لاحقاً)
                val parsedGenres = parseMultipleItems("Unknown Genre")

                // 3. حفظ أو تحديث الألبوم أولاً
                val albumEntity = AlbumEntity(
                    albumId = albumId,
                    title = albumTitle,
                    albumArtist = parsedArtists.firstOrNull() ?: "Unknown Artist",
                    coverPath = null, // سيتم استخراجه ديناميكيًا في الـ UI أو الـ Playback
                    year = year
                )
                musicDao.insertAlbum(albumEntity)

                // 4. حفظ الأغنية الأساسية
                val songEntity = SongEntity(
                    songId = id,
                    title = title,
                    albumId = albumId,
                    albumTitle = albumTitle,
                    duration = duration,
                    trackNumber = trackNumber % 1000, // لتجنب أكواد الـ CD المشتركة
                    discNumber = trackNumber / 1000,
                    year = year,
                    composer = "Unknown Composer",
                    lyrics = null, // يضاف عند التعديل أو القراءة من ملف الـ LRC
                    path = path,
                    dateAdded = dateAdded
                )
                musicDao.insertSong(songEntity)

                // 5. ربط الأغنية بجميع الفنانين المفككين (Many-to-Many)
                parsedArtists.forEach { artistName ->
                    val artistId = artistName.lowercase().trim().replace(" ", "_")
                    val artistEntity = ArtistEntity(artistId = artistId, name = artistName.trim())
                    musicDao.insertArtist(artistEntity)
                    musicDao.insertSongArtistCrossRef(SongArtistCrossRef(id, artistId))
                }

                // 6. ربط الأغنية بجميع الأنواع الموسيقية (Many-to-Many)
                parsedGenres.forEach { genreName ->
                    val genreId = genreName.lowercase().trim().replace(" ", "_")
                    val genreEntity = GenreEntity(genreId = genreId, name = genreName.trim())
                    musicDao.insertGenre(genreEntity)
                    musicDao.insertSongGenreCrossRef(SongGenreCrossRef(id, genreId))
                }
            }
        }
    }

    // دالة سحرية لتقسيم النصوص بناءً على الفواصل القياسية لـ Multiple Tags
    private fun parseMultipleItems(rawText: String): List<String> {
        if (rawText.isBlank()) return listOf("Unknown")
        // الفواصل المعتمدة: غيبوبة، شرطة مائلة، وكلمات الربط الشهيرة مثل feat أو ft وعلامة &
        val delimiters = regexSplitPattern
        return rawText.split(delimiters)
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("feat", ignoreCase = true) && !it.equals("ft", ignoreCase = true) }
    }

    companion object {
        private val regexSplitPattern = Regex(",|/|;|\\s+&\\s+|\\s+[Ff]eat\\.?\\s+|\\s+[Ff]t\\.?\\s+")
    }
}
