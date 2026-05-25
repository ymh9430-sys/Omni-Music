package com.example.materialyouplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.materialyouplayer.data.database.SongEntity
import com.example.materialyouplayer.data.database.SongWithDetails
import com.example.materialyouplayer.data.repository.MusicRepository
import com.example.materialyouplayer.playback.PlaybackManager
import com.example.materialyouplayer.playback.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val playbackManager = PlaybackManager(application, repository)

    // ==========================================
    // تدفق البيانات الحية (UI State Flows) من قاعدة البيانات
    // ==========================================

    val allSongs: StateFlow<List<SongWithDetails>> = repository.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<SongWithDetails>> = repository.getRecentlyAdded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbums = repository.getAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtists = repository.getAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenres = repository.getAllGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // مراقبة حالة المشغل بالميلي ثانية فوريًا للـ UI والـ Karaoke
    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    init {
        // فحص الملفات وتحديث المكتبة تلقائيًا عند تشغيل التطبيق أول مرة
        refreshMediaLibrary()
    }

    fun refreshMediaLibrary() {
        viewModelScope.launch {
            repository.refreshMediaLibrary()
        }
    }

    // ==========================================
    // دوال التحكم في تشغيل الصوت (Playback Controls)
    // ==========================================

    fun playSongs(songs: List<SongWithDetails>, startIndex: Int = 0) {
        playbackManager.playSongs(songs, startIndex)
    }

    fun togglePlayPause() {
        if (playbackState.value.isPlaying) {
            playbackManager.pause()
        } else {
            playbackManager.play()
        }
    }

    fun skipToNext() = playbackManager.skipToNext()
    
    fun skipToPrevious() = playbackManager.skipToPrevious()

    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)

    fun setShuffleMode(enabled: Boolean) = playbackManager.setShuffleMode(enabled)

    fun setRepeatMode(mode: Int) = playbackManager.setRepeatMode(mode)

    // ==========================================
    // دوال الـ Tag Editor وتعديل الكلمات (Lyrics)
    // ==========================================

    fun updateLyrics(songId: String, newLyrics: String?) {
        viewModelScope.launch {
            repository.updateSongLyrics(songId, newLyrics)
        }
    }

    fun updateSongTags(
        songEntity: SongEntity,
        artistNames: List<String>,
        genreNames: List<String>
    ) {
        viewModelScope.launch {
            repository.updateSongTags(songEntity, artistNames, genreNames)
        }
    }
}
