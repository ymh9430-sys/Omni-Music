package com.example.materialyouplayer.playback

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        // بناء مشغل الـ ExoPlayer الاحترافي وتجهيزه للأداء العالي
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true) // التعامل الذكي مع الفوكس (لو مكالمة جت الصوت يقف تلقائي)
            .build()

        // إنشاء الـ MediaSession وربطه بالـ Player لإدارة التحكم الخارجي
        player?.let { exoPlayer ->
            mediaSession = MediaSession.Builder(this, exoPlayer).build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // الحفاظ على موارد الموبايل ومسح الكاش عند إغلاق التطبيق نهائيًا من الخلفية
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }
}
