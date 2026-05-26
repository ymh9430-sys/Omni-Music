package com.example.materialyouplayer.ui.navigation

import android.content.ContentUris
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// دالة مساعدة لتحويل الـ Album ID إلى رابط صورة حقيقي يتعرف عليه الـ Coil فوراً
fun getAlbumArtUri(albumId: Long): Uri {
    return ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        albumId
    )
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    viewModel: MainViewModel // همرر الـ ViewModel بتاعك هنا عشان ناخد منه البيانات المتصفية
) {
    // جلب الحالات والـ Toggles من الـ ViewModel
    val allSongs by viewModel.allSongs.collectAsState(initial = emptyList())
    val allAlbums by viewModel.allAlbums.collectAsState(initial = emptyList())
    val includeSingles by viewModel.includeSingles.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        // 1. شاشة الـ Home
        composable("home") {
            // تصفية أول 10 أغانٍ فقط للرئيسية
            val homeSongs = allSongs.take(10)
            HomeScreen(
                songs = homeSongs,
                onSeeAllClick = { navController.navigate("recently_added_all") },
                onSongClick = { song -> viewModel.playSong(song) }
            )
        }

        // شاشة فرعية لعرض آخر 100 أغنية عند الضغط على السهم الأخضر
        composable("recently_added_all") {
            val viewAllSongs = allSongs.take(100)
            RecentlyAddedAllScreen(
                songs = viewAllSongs,
                onBackClick = { navController.popBackStack() },
                onSongClick = { song -> viewModel.playSong(song) }
            )
        }

        // 2. شاشة الـ Albums
        composable("albums") {
            // تصفية الألبومات بناءً على خيار include singles أو عدد الأغاني المتاحة
            val filteredAlbums = if (includeSingles) {
                allAlbums
            } else {
                allAlbums.filter { it.songCount > 1 }
            }

            AlbumsScreen(
                albums = filteredAlbums,
                includeSingles = includeSingles,
                onToggleSingles = { viewModel.toggleIncludeSingles() },
                onAlbumClick = { album -> navController.navigate("album_details/${album.id}") }
            )
        }

        // 3. شاشة الـ Songs
        composable("songs") {
            SongsScreen(
                songs = allSongs,
                onSongClick = { song -> viewModel.playSong(song) }
            )
        }

        // 4. شاشة الـ Playlists
        composable("playlists") {
            PlaylistsScreen(viewModel = viewModel)
        }

        // 5. شاشة الـ Artists
        composable("artists") {
            ArtistsScreen(viewModel = viewModel)
        }
    }
}
