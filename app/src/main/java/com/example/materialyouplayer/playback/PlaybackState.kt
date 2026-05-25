package com.example.materialyouplayer.playback

import com.example.materialyouplayer.data.database.SongWithDetails

data class PlaybackState(
    val currentSong: SongWithDetails? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = 0 // 0 = OFF, 1 = ONE, 2 = ALL
)
