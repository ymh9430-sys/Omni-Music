package com.example.materialyouplayer.ui.screens.home

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.materialyouplayer.ui.viewmodel.MainViewModel
import com.example.materialyouplayer.data.database.SongWithDetails

// دالة مساعدة لجلب رابط صورة الألبوم محلياً من نظام أندرويد
fun getAlbumArtUri(albumId: Long): Uri {
    return ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    )
}

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToRecentlyAddedAll: () -> Unit
) {
    // مراقبة البيانات الحية القادمة من السكنر وقاعدة البيانات
    val songs by viewModel.allSongs.collectAsState(initial = emptyList())
    val recentlyAdded by viewModel.recentlyAdded.collectAsState(initial = emptyList())
    val albums by viewModel.allAlbums.collectAsState(initial = emptyList())
    val artists by viewModel.allArtists.collectAsState(initial = emptyList())

    val pureBlack = Color(0xFF000000)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(pureBlack)
            .padding(bottom = 80.dp) // مساحة إضافية حتى لا يغطي الـ Mini Player على المحتوى
    ) {
        
        // 1. شريط البحث العلوي (Search Bar) المطابق تمامًا لتصميمك
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFF161616)) // الرمادي الداكن الصريح من صورتك
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Search your music",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // 2. الأزرار الدائرية الأربعة (Quick Actions)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton("History", Icons.Default.Refresh, Color(0xFF448AFF)) {}
                QuickActionButton("Favorites", Icons.Default.Favorite, Color(0xFFFF4081)) {}
                QuickActionButton("Most played", Icons.Default.ThumbUp, Color(0xFF00E676)) {}
                QuickActionButton("Shuffle", Icons.Default.Share, Color(0xFFFFD700)) {
                    if (songs.isNotEmpty()) viewModel.setShuffleMode(true)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. قسم الـ Recently added songs (يعرض أول 10 أغانٍ فقط مع السهم الأخضر للتنقل)
        item {
            SectionHeader(
                title = "Recently added songs", 
                onSeeAllClick = onNavigateToRecentlyAddedAll
            )
            if (recentlyAdded.isEmpty()) {
                Text(
                    text = "No recent songs found",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // تطبيق شرط عرض أول 10 أغاني فقط في شاشة الهوم الرئيسية
                    items(recentlyAdded.take(10)) { songDetails ->
                        WideSongCard(songDetails = songDetails) {
                            viewModel.playSongs(recentlyAdded, recentlyAdded.indexOf(songDetails))
                        }
                    }
                }
            }
        }

        // 4. قسم الـ Recently played albums (كروت مربعة وتدعم جلب الغلاف)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Recently played albums", onSeeAllClick = {})
            if (albums.isEmpty()) {
                Text(
                    text = "No albums found",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(albums) { albumWithSongs ->
                        SquareAlbumCard(
                            title = albumWithSongs.album.title,
                            subtitle = albumWithSongs.album.albumArtist,
                            albumId = albumWithSongs.album.albumId
                        ) {
                            onNavigateToAlbum(albumWithSongs.album.albumId)
                        }
                    }
                }
            }
        }

        // 5. قسم الـ Recent Artists (دوائر كاملة وصافية للفنانين)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Recent Artists", onSeeAllClick = {})
            if (artists.isEmpty()) {
                Text(
                    text = "No artists found",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artists) { artistWithSongs ->
                        CircleArtistCard(name = artistWithSongs.artist.name) {
                            onNavigateToArtist(artistWithSongs.artist.artistId)
                        }
                    }
                }
            }
        }

        // 6. قسم الـ Favorites (كروت مربعة للأغاني المفضلة مع جلب الأغلفة)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(title = "Favorites", onSeeAllClick = {})
            if (songs.isEmpty()) {
                Text(
                    text = "No favorites yet",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(songs.take(5)) { songDetails ->
                        SquareAlbumCard(
                            title = songDetails.song.title,
                            subtitle = songDetails.artists.joinToString(", ") { it.name },
                            albumId = songDetails.song.albumId
                        ) {
                            viewModel.playSongs(songs, songs.indexOf(songDetails))
                        }
                    }
                }
            }
        }
    }
}

// مكون عنوان القسم مع السهم الأخضر لفتح الـ 100 أغنية كاملين
@Composable
fun SectionHeader(title: String, onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onSeeAllClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "See All",
                tint = Color(0xFF22C55E) // السهم الأخضر من السكرينات بالظبط
            )
        }
    }
}

// كرت الأغنية العريض مع تفعيل كود الـ Coil لقراءة صور الألبومات المحلية
@Composable
fun WideSongCard(songDetails: SongWithDetails, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = getAlbumArtUri(songDetails.song.albumId),
            contentDescription = "Song Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1C1C1C)) // باك اب لو مفيش غلاف مدمج
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = songDetails.song.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = songDetails.artists.joinToString(", ") { it.name },
                color = Color.Gray,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// الكرت المربع الخاص بالألبومات والمفضلة مع تفعيل معالج الصور المدمجة
@Composable
fun SquareAlbumCard(title: String, subtitle: String, albumId: Long, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = getAlbumArtUri(albumId),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1C))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
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

// كرت الفنان الدائري النظيف
@Composable
fun CircleArtistCard(name: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1C)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// الأزرار الدائرية الأربعة العلوية
@Composable
fun QuickActionButton(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0xFF161616)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
