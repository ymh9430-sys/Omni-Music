package com.example.materialyouplayer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.ui.viewmodel.MainViewModel

@Composable
fun FullPlayerScreen(
    viewModel: MainViewModel,
    dragProgress: Float, // نسبة السحب الحالية لإدارة الأنيميشن لحظة بلحظة
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong ?: return

    // احتساب الوقت المنقضي والمتبقي بدقة الميلي ثانية للشريط
    val currentPosMs = playbackState.currentPosition
    val durationMs = playbackState.duration
    val progress = if (durationMs > 0) currentPosMs.toFloat() / durationMs.toFloat() else 0f

    val timeElapsed = formatTime(currentPosMs)
    val timeRemaining = formatTime(if (durationMs > 0) (durationMs - currentPosMs) else 0L)

    // ========================================================
    // حسابات أنيميشن الـ Apple Music (Scale & Translation)
    // ========================================================
    // الكفر بيبدأ بحجم صغير (مثلاً 0.6f) ويكبر لحد الحجم الكامل 1.0f بناءً على السحب
    val albumCardScale = 0.6f + (dragProgress * 0.4f)
    // تهبيط الكفر لأسفل قليلاً أثناء السحب ليعطي تأثير العمق الحركي
    val albumTranslationY = (1f - dragProgress) * 150f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 24.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // خط علوي صغير (Handle الـ Sheet) للإشارة بقابلية السحب لأسفل
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.DarkGray.copy(alpha = 0.5f))
        )

        Spacer(modifier = Modifier.weight(0.2f))

        // 1. كفر الأغنية المربع السحري الخاضع لأنيميشن الـ Apple Music بالملي ثانية
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

        Spacer(modifier = Modifier.weight(0.3f))

        // 2. تفاصيل الأغنية (العنوان والفنان والـ Badges) المطابقة تماماً لصورتك
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
                    // الـ Explicit Badge [E] الرمادي الأنيق في تصميمك
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
                Text(
                    text = currentSong.artists.joinToString(", ") { it.name },
                    color = Color.Gray,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // أزرار النجمة والقائمة الجانبية المستوحاة من لقطة شاشتك
            Row {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Favorite", tint = Color.Gray)
                }
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. شريط التقدم الـ Minimalist والوقت (المنقضي / المتبقي) بالملي ثانية
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
                Text(text = "-$timeRemaining", color = Color.Gray, fontSize = 12.sp) // التوقيت المتبقي بالسالب كصورتك
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        // 4. أزرار التحكم الكبيرة والأساسية (التشغيل والانتقال)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipToPrevious() }, modifier = Modifier.size(64.dp)) {
                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.width(32.dp))
            
            // زر التشغيل الدائري العملاق في المنتصف
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

        Spacer(modifier = Modifier.weight(0.4f))

        // 5. الأزرار السفلية (الكلمات Lyrics والـ Queue القائمة) كما في آخر لقطة شاشة بعتها
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // أيقونة الـ Lyrics (الدردشة النصية) جهة اليسار
            IconButton(onClick = { /* سنفتح منها شاشة الـ Karaoke لاحقاً */ }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Refresh, // أيقونة مؤقتة للـ Lyrics Chat
                    contentDescription = "Lyrics",
                    tint = Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            // أيقونة الـ Queue (ثلاثة أسطر متوازية) جهة اليمين
            IconButton(onClick = { }) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.List,
                    contentDescription = "Queue",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// دالة مساعدة لتحويل الميلي ثانية لشكل قياسي مريح للعين mm:ss
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%01d:%02d", minutes, seconds)
}
