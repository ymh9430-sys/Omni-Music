package com.example.materialyouplayer.ui.screens.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.materialyouplayer.data.lyrics.LrcParser
import com.example.materialyouplayer.data.lyrics.LyricLine
import com.example.materialyouplayer.ui.theme.PureBlack
import com.example.materialyouplayer.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class) // السماح باستخدام ميزات التخطيط التجريبية على السيرفر
@Composable
fun LyricsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playbackState.collectAsState()
    val currentSong = playbackState.currentSong
    val currentPosition = playbackState.currentPosition

    val lyricLines = remember(currentSong?.song?.lyrics) {
        LrcParser.parseLyrics(currentSong?.song?.lyrics)
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentLineIndex = remember(currentPosition, lyricLines) {
        lyricLines.indexOfFirst { currentPosition in it.startTime..it.endTime }
    }

    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex != -1) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = (currentLineIndex - 2).coerceAtLeast(0)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(horizontal = 20.dp)
    ) {
        if (lyricLines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No lyrics available", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 100.dp, bottom = 150.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                itemsIndexed(lyricLines) { index, line ->
                    val isCurrentLine = index == currentLineIndex
                    
                    KaraokeLineRow(
                        line = line,
                        currentPosition = currentPosition,
                        isCurrentLine = isCurrentLine,
                        onClick = {
                            viewModel.seekTo(line.startTime)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KaraokeLineRow(
    line: LyricLine,
    currentPosition: Long,
    isCurrentLine: Boolean,
    onClick: () -> Unit
) {
    val fontSize = if (isCurrentLine) 26.sp else 22.sp
    val fontWeight = if (isCurrentLine) FontWeight.ExtraBold else FontWeight.Bold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        if (line.words.isEmpty()) {
            Text(
                text = line.text,
                color = if (isCurrentLine) Color.White else Color.Gray.copy(alpha = 0.5f),
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        } else {
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                line.words.forEach { wordInfo ->
                    val wordColor = when {
                        currentPosition >= wordInfo.endTime -> Color.White
                        currentPosition in wordInfo.startTime..wordInfo.endTime -> Color.White
                        else -> Color.Gray.copy(alpha = 0.3f)
                    }

                    Text(
                        text = wordInfo.word + " ",
                        color = wordColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    )
                }
            }
        }
    }
}
