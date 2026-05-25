package com.example.materialyouplayer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.ui.viewmodel.MainViewModel

@Composable
fun MiniPlayer(
    viewModel: MainViewModel,
    onMiniPlayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // مراقبة حالة المشغل والأغنية الحالية بالميلي ثانية فوريًا
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong ?: return // إذا لم تكن هناك أغنية تعمل، يختفي المشغل تلقائيًا

    // احتساب نسبة التقدم بدقة الميلي ثانية
    val progress = if (playbackState.duration > 0) {
        playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp) // لجعله عائماً بحواف ناعمة فوق البار السفلي كما في صورتك
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111)) // الرمادي الداكن المخصص للمشغل من صورتك
            .clickable { onMiniPlayerClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. صورة كفر الأغنية (مربع بحواف دائرية ناعمة)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. نصوص الأغنية والفنان
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSong.song.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = currentSong.artists.joinToString(", ") { it.name },
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. أزرار التحكم الـ Minimalist جهة اليمين
            IconButton(onClick = { viewModel.skipToPrevious() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = { viewModel.togglePlayPause() }) {
                Icon(
                    imageVector = if (playbackState.isPlaying) {
                        // أيقونة الإيقاف المؤقت
                        androidx.compose.material.icons.Icons.Default.Menu // سنستبدلها بـ Pause قياسي لاحقاً
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(onClick = { viewModel.skipToNext() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 4. خط التقدم الأخضر النحيف الممتد بالكامل أسفل المشغل الصغير (المطابق لصورتك)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialGreen, // اللون الأخضر الماتيريال المختار من تصميمك
            trackColor = Color.DarkGray.copy(alpha = 0.3f)
        )
    }
}
