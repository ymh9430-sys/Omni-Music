package com.example.materialyouplayer.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.materialyouplayer.data.database.SongWithDetails
import com.example.materialyouplayer.data.repository.MusicRepository
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackManager(
    private val context: Context,
    private val repository: MusicRepository
) {
    private var mediaController: MediaController? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    // قائمة الأغاني الحالية المفعلة في طابور التشغيل (Queue)
    private var currentQueue: List<SongWithDetails> = emptyList()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get().apply {
                    addListener(playerListener)
                    syncState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            syncState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncState()
            if (isPlaying) {
                startTrackingPosition()
            } else {
                stopTrackingPosition()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val currentId = mediaItem?.mediaId
            if (currentId != null) {
                repositoryScope.launch {
                    val songDetails = repository.getSongById(currentId)
                    _playbackState.update { it.copy(currentSong = songDetails) }
                }
            } else {
                _playbackState.update { it.copy(currentSong = null) }
            }
            syncState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _playbackState.update { it.copy(shuffleModeEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _playbackState.update { it.copy(repeatMode = repeatMode) }
        }
    }

    // لتشغيل قائمة أغاني واختيار أغنية محددة تبدأ منها
    fun playSongs(songs: List<SongWithDetails>, startIndex: Int = 0) {
        val controller = mediaController ?: return
        currentQueue = songs
        controller.stop()
        controller.clearMediaItems()

        val mediaItems = songs.map { songDetails ->
            MediaItem.Builder()
                .setMediaId(songDetails.song.songId)
                .setUri(songDetails.song.path)
                .build()
        }

        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun play() { mediaController?.play() }
    fun pause() { mediaController?.pause() }
    fun seekTo(positionMs: Long) { mediaController?.seekTo(positionMs) }
    fun skipToNext() { mediaController?.seekToNext() }
    fun skipToPrevious() { mediaController?.seekToPrevious() }

    fun setShuffleMode(enabled: Boolean) { mediaController?.shuffleModeEnabled = enabled }
    fun setRepeatMode(mode: Int) { mediaController?.repeatMode = mode }

    // مزامنة حالة المشغل الحالية مع الـ StateFlow لتحديث الـ UI فوريًا
    private fun syncState() {
        val controller = mediaController ?: return
        _playbackState.update {
            it.copy(
                isPlaying = controller.isPlaying,
                currentPosition = controller.currentPosition,
                duration = controller.duration.coerceAtLeast(0L),
                shuffleModeEnabled = controller.shuffleModeEnabled,
                repeatMode = controller.repeatMode
            )
        }
    }

    // عدّاد تكراري فائق السرعة (كل 16 ميلي ثانية) لتحديث شريط التقدم والـ Karaoke بدون أي تكتكة
    private fun startTrackingPosition() {
        positionUpdateJob?.cancel()
        positionUpdateJob = repositoryScope.launch(Dispatchers.Main) {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _playbackState.update { 
                            it.copy(currentPosition = controller.currentPosition) 
                        }
                    }
                }
                delay(16L) // متوافق مع سرعة تحديث الشاشات (60Hz/120Hz) للأنيميشن السلس
            }
        }
    }

    private fun stopTrackingPosition() {
        positionUpdateJob?.cancel()
    }
}
