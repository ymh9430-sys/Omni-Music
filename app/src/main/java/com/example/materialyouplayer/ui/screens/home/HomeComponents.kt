package com.example.materialyouplayer.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.data.database.SongWithDetails

// الألوان المستوحاة مباشرة من تصميمك المرفق
val DarkGrayCard = Color(0xFF121212)
val PureBlack = Color(0xFF000000)
val MaterialGreen = Color(0xFF00C853)
val DimCircleBackground = Color(0xFF1A1A1A)

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(DimCircleBackground)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        // السهم الأخضر المميز في تصميمك
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
            contentDescription = "See All",
            tint = MaterialGreen,
            modifier = Modifier
                .size(24.dp)
                .clickable { onSeeAllClick() }
        )
    }
}

@Composable
fun WideSongCard(songDetails: SongWithDetails, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(240.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0288D1)) // خلفية زرقاء ديناميكية كمثال لـ In the Name of Love
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.7f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = songDetails.song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = songDetails.artists.joinToString(", ") { it.name },
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // زر الـ Play الشفاف المتموضع في المنتصف/اليمين
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SquareAlbumCard(title: String, subtitle: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            // زر الـ Play الصغير الشفاف أسفل اليسار كما في صورتك
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp)
                    .align(Alignment.BottomStart)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CircleArtistCard(name: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
