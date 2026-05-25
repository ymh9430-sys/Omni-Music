package com.example.materialyouplayer.ui.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.ui.viewmodel.MainViewModel
import com.example.materialyouplayer.ui.theme.PureBlack
import com.example.materialyouplayer.ui.theme.MaterialGreen // استيراد اللون الأخضر الماتيريال

enum class DragStates { COLLAPSED, EXPANDED }

@OptIn(ExperimentalFoundationApi::class) // السماح بالـ AnchoredDraggable التجريبي لمنع توقف الـ Actions
@Composable
fun MainNavigationContainer(
    viewModel: MainViewModel,
    homeScreenContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val collapsedHeightPx = with(density) { 140.dp.toPx() }

    val draggableState = remember {
        @Suppress("DEPRECATION")
        AnchoredDraggableState(
            initialValue = DragStates.COLLAPSED,
            anchors = DraggableAnchors {
                DragStates.COLLAPSED at screenHeightPx - collapsedHeightPx
                DragStates.EXPANDED at 0f
            },
            positionalThreshold = { distance -> distance * 0.4f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween(durationMillis = 350)
        )
    }

    val dragProgress by remember {
        derivedStateOf {
            val collapsedOffset = screenHeightPx - collapsedHeightPx
            val currentOffset = draggableState.offset
            if (collapsedOffset - currentOffset > 0) {
                ((collapsedOffset - currentOffset) / collapsedOffset).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PureBlack)) {
        Box(modifier = Modifier.fillMaxSize()) {
            homeScreenContent()
        }

        val playbackState by viewModel.playbackState.collectAsState()

        if (playbackState.currentSong != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = with(density) { draggableState.offset.toDp() })
                    .anchoredDraggable(
                        state = draggableState,
                        orientation = Orientation.Vertical
                    )
                    .background(
                        Color(0xFF151515).copy(alpha = dragProgress.coerceAtLeast(0.9f))
                    )
            ) {
                if (dragProgress < 0.2f) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onMiniPlayerClick = {}
                    )
                } else {
                    FullPlayerScreen(
                        viewModel = viewModel,
                        dragProgress = dragProgress
                    )
                }
            }
        }

        if (dragProgress < 0.9f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        alpha = 1f - (dragProgress * 2f).coerceIn(0f, 1f)
                        translationY = dragProgress * 100f
                    }
                    .width(340.dp)
                    .height(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF111111))
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavigationItem("Home", Icons.Default.Home, isSelected = true) {}
                    NavigationItem("Library", Icons.Default.LibraryMusic, isSelected = false) {}
                    NavigationItem("Artists", Icons.Default.Person, isSelected = false) {}
                }
            }
        }
    }
}

@Composable
fun NavigationItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF1E3524) else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialGreen else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        if (isSelected) {
            Text(text = label, color = MaterialGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}
