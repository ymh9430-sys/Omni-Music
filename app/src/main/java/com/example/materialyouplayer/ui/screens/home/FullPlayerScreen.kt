package com.example.materialyouplayer.ui.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.ui.screens.lyrics.LyricsScreen
import com.example.materialyouplayer.ui.viewmodel.MainViewModel

@Composable
fun FullPlayerScreen(
    viewModel: MainViewModel,
    dragProgress: Float,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong ?: return

    var showLyrics by remember { mutableStateOf(false) }

    val currentPosMs = playbackState.currentPosition
    val durationMs = playbackState.duration
    val progress = if (durationMs > 0) currentPosMs.toFloat() / durationMs.toFloat() else 0f

    val timeElapsed = formatTime(currentPosMs)
    val timeRemaining = formatTime(if (durationMs > 0) (durationMs - currentPosMs) else 0L)

    val albumCardScale = 0.6f + (dragProgress * 0.4f)
    val albumTranslationY = (1f - dragProgress) * 150f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // خط علوي صغير للسحب
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f))
        )

        // تبديل المحتوى بسلاسة (Crossfade) بين الكفر وشاشة الـ Lyrics
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = showLyrics, label = "PlayerContent") { lyricsVisible ->
                if (lyricsVisible) {
                    // عرض شاشة الكلمات المزامنة كلمة بكلمة
                    LyricsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // الواجهة التقليدية: عرض الكفر ومعلومات الأغنية
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(310.dp)
                                .graphicsLayer {
                                    scaleX = albumCardScale
                                    scaleY = albumCardScale
                                    translationY = albumTranslationY
                                }
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1E1E1E))
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentSong.song.title,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.DarkGray)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(text = "E", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                NavigableArtistRow(viewModel = viewModel, songDetails = currentSong)
                            }

                            Row {
                                IconButton(onClick = {}) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", tint = Color.Gray)
                                }
                                IconButton(onClick = {}) {
                                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. شريط التقدم والوقت (ثابت بالأسفل في الحالتين لسهولة التحكم)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val seekTarget = (newProgress * durationMs).toLong()
                    viewModel.seekTo(seekTarget)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White.copy(alpha = 0.8f),
                    inactiveTrackColor = Color.DarkGray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = timeElapsed, color = Color.Gray, fontSize = 12.sp)
                Text(text = "-$timeRemaining", color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. أزرار التحكم الكبيرة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(64.dp)) {
                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(32.dp))
            
            Surface(
                onClick = { viewModel.togglePlayPause() },
                shape = RoundedCornerShape(percent = 50),
                color = Color.White,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (playbackState.isPlaying) Icons.Default.MoreVert else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(32.dp))
            IconButton(onClick = { viewModel.skipToNext() }, modifier = Modifier.size(64.dp)) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. الأزرار السفلية (التحكم في فتح وغلق الـ Lyrics فوريًا)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زرار الـ Lyrics يتحول للون الأخضر عند التفعيل لإعطاء مظهر متفاعل واحترافي
            IconButton(onClick = { showLyrics = !showLyrics }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Lyrics",
                    tint = if (showLyrics) MaterialGreen else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Queue",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun NavigableArtistRow(viewModel: MainViewModel, songDetails: com.example.materialyouplayer.data.database.SongWithDetails) {
    Text(
        text = songDetails.artists.joinToString(", ") { it.name },
        color = Color.Gray,
        fontSize = 16.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%01d:%02d", minutes, seconds)
}
