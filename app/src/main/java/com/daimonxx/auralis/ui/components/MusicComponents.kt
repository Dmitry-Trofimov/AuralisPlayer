package com.daimonxx.auralis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daimonxx.auralis.data.AudioTrack
import com.daimonxx.auralis.ui.theme.AuralisColors

@Composable
fun TrackRow(
    track: AudioTrack,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(
            artworkUri = track.artworkUri,
            modifier = Modifier.size(54.dp),
            iconSize = 23.dp
        )
        Column(
            modifier = Modifier.weight(1f).padding(start = 13.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.title,
                color = if (isCurrent) AuralisColors.PurpleBright else AuralisColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "${track.artist}  ·  ${track.album}",
                color = AuralisColors.Muted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.relativePath,
                color = AuralisColors.Muted.copy(alpha = 0.62f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatDuration(track.durationMs),
            color = AuralisColors.Muted,
            fontSize = 11.sp,
            modifier = Modifier.padding(end = 1.dp)
        )
        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(34.dp)) {
            Icon(
                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = if (isFavorite) "Убрать из избранного" else "Добавить в избранное",
                tint = if (isFavorite) Color(0xFFFF9ECF) else AuralisColors.Muted.copy(alpha = 0.70f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MiniPlayer(
    title: String,
    artist: String,
    artworkUri: android.net.Uri?,
    isPlaying: Boolean,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        cornerRadius = 27.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(start = 10.dp, end = 12.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(artworkUri = artworkUri, modifier = Modifier.size(50.dp), iconSize = 22.dp)
            Column(Modifier.weight(1f).padding(start = 11.dp)) {
                Text(title, color = AuralisColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist, color = AuralisColors.Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                    tint = AuralisColors.Text
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Следующий трек", tint = AuralisColors.Text)
            }
        }
    }
}

@Composable
fun BottomDock(selected: Int, onSelect: (Int) -> Unit) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        cornerRadius = 38.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockIcon(selected == 0, Icons.Rounded.Home, "Главная") { onSelect(0) }
            DockIcon(selected == 1, Icons.Rounded.PlaylistPlay, "Медиатека") { onSelect(1) }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFFB9A0FF), Color(0xFF65429F), Color(0xFF20234E)))
                    )
                    .clickable { onSelect(2) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Album, contentDescription = "Плеер", tint = Color.White, modifier = Modifier.size(31.dp))
            }

            DockIcon(selected == 3, Icons.Rounded.GraphicEq, "Звук") { onSelect(3) }
            DockIcon(selected == 4, Icons.Rounded.NotificationsNone, "Таймер") { onSelect(4) }
        }
    }
}

@Composable
private fun DockIcon(selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0x2DB99AFF) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (selected) AuralisColors.PurpleBright else Color(0xFF81798D),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun AlbumTile(title: String, artist: String, artworkUri: android.net.Uri?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Artwork(artworkUri = artworkUri, modifier = Modifier.fillMaxWidth().height(148.dp), iconSize = 34.dp)
        Text(title, color = AuralisColors.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp, start = 2.dp))
        Text(artist, color = AuralisColors.Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp, start = 2.dp))
    }
}

@Composable
fun EmptyLibrary(onGrantAccess: () -> Unit, message: String = "Музыка пока не найдена") {
    GlassSurface(modifier = Modifier.fillMaxWidth().padding(20.dp), cornerRadius = 28.dp) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Album, contentDescription = null, tint = AuralisColors.Purple, modifier = Modifier.size(44.dp))
            Text(message, color = AuralisColors.Text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            Text("Разреши доступ к аудио, чтобы Auralis нашёл музыку в памяти устройства.", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
            Spacer(Modifier.height(14.dp))
            androidx.compose.material3.Button(onClick = onGrantAccess) {
                Text("Открыть доступ")
            }
        }
    }
}

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
