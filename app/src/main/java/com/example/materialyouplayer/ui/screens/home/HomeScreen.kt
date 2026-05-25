package com.example.materialyouplayer.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit
) {
    // مراقبة البيانات الحية القادمة من السكنر وقاعدة البيانات
    val songs by viewModel.allSongs.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val albums by viewModel.allAlbums.collectAsState()
    val artists by viewModel.allArtists.collectAsState()

    // واجهة القائمة الرأسية الشاملة لمنع أي بطء في الشاشة (True Black Background)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
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

        // 3. قسم الـ Recently added songs (كروت مستطيلة عريضة)
        item {
            SectionHeader(title = "Recently added songs", onSeeAllClick = {})
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
                    items(recentlyAdded) { songDetails ->
                        WideSongCard(songDetails = songDetails) {
                            // عند الضغط يتم تشغيل الأغنية فورًا من طابور الأغاني الحديثة
                            viewModel.playSongs(recentlyAdded, recentlyAdded.indexOf(songDetails))
                        }
                    }
                }
            }
        }

        // 4. قسم الـ Recently played albums (كروت مربعة)
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
                            subtitle = albumWithSongs.album.albumArtist
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

        // 6. قسم الـ Favorites (كروت مربعة للأغاني المفضلة)
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
                    // نستخدم قائمة الأغاني العامة مؤقتًا كعرض في المفضلة
                    items(songs.take(5)) { songDetails ->
                        SquareAlbumCard(
                            title = songDetails.song.title,
                            subtitle = songDetails.artists.joinToString(", ") { it.name }
                        ) {
                            viewModel.playSongs(songs, songs.indexOf(songDetails))
                        }
                    }
                }
            }
        }
    }
}
