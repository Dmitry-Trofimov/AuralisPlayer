package com.daimonxx.auralis.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import com.daimonxx.auralis.playback.AuralisEqualizer
import com.daimonxx.auralis.data.AudioRepository
import com.daimonxx.auralis.data.AudioTrack
import com.daimonxx.auralis.ui.components.AlbumTile
import com.daimonxx.auralis.ui.components.Artwork
import com.daimonxx.auralis.ui.components.BottomDock
import com.daimonxx.auralis.ui.components.EmptyLibrary
import com.daimonxx.auralis.ui.components.GlassSurface
import com.daimonxx.auralis.ui.components.GlowOrb
import com.daimonxx.auralis.ui.components.MiniEqualizerBars
import com.daimonxx.auralis.ui.components.MiniPlayer
import com.daimonxx.auralis.ui.components.TrackRow
import com.daimonxx.auralis.ui.components.formatDuration
import com.daimonxx.auralis.ui.theme.AuralisColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_HOME = 0
private const val PAGE_LIBRARY = 1
private const val PAGE_PLAYER = 2
private const val PAGE_SOUND = 3
private const val PAGE_TIMER = 4

@OptIn(UnstableApi::class)
@Composable
fun AuralisApp(controller: MediaController?) {
    val context = LocalContext.current
    val audioPermission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var tracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }
    val preferences = remember(context) { context.getSharedPreferences("auralis_preferences", 0) }
    var equalizerEnabled by remember { mutableStateOf(preferences.getBoolean("eq_enabled", true)) }
    var equalizerGains by remember {
        mutableStateOf(AuralisEqualizer.parseProfile(preferences.getString("eq_gains", null)))
    }
    var equalizerPresetId by remember { mutableStateOf(preferences.getString("eq_preset", "flat") ?: "flat") }
    var favoriteIds by remember {
        mutableStateOf(preferences.getStringSet("favorite_ids", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet())
    }
    var loading by remember { mutableStateOf(false) }
    var page by rememberSaveable { mutableIntStateOf(PAGE_PLAYER) }
    var currentTrackId by remember { mutableStateOf<String?>(null) }
    var currentTitle by remember { mutableStateOf("Выбери композицию") }
    var currentArtist by remember { mutableStateOf("Твоя музыка — прямо на устройстве") }
    var currentArtwork by remember { mutableStateOf<Uri?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableIntStateOf(Player.REPEAT_MODE_OFF) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    var timerMinutes by rememberSaveable { mutableIntStateOf(30) }
    var timerActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val audioPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasAudioPermission = granted
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            loading = true
            runCatching { AudioRepository(context).scanDevice() }
                .onSuccess { tracks = it }
            loading = false
        }
    }

    LaunchedEffect(equalizerEnabled, equalizerGains, equalizerPresetId) {
        preferences.edit()
            .putBoolean("eq_enabled", equalizerEnabled)
            .putString("eq_gains", AuralisEqualizer.serializeProfile(equalizerGains))
            .putString("eq_preset", equalizerPresetId)
            .apply()
    }

    LaunchedEffect(controller, equalizerEnabled, equalizerGains) {
        controller?.let { mediaController ->
            val command = SessionCommand(AuralisEqualizer.ACTION_SET_PROFILE, Bundle.EMPTY)
            val arguments = Bundle().apply {
                putBoolean(AuralisEqualizer.EXTRA_ENABLED, equalizerEnabled)
                putFloatArray(AuralisEqualizer.EXTRA_GAINS, equalizerGains.toFloatArray())
            }
            runCatching { mediaController.sendCustomCommand(command, arguments) }
        }
    }

    LaunchedEffect(controller, tracks) {
        while (true) {
            val player = controller
            if (player != null) {
                val item = player.currentMediaItem
                val metadata = item?.mediaMetadata
                currentTrackId = item?.mediaId
                currentTitle = metadata?.title?.toString()?.takeIf { it.isNotBlank() } ?: "Выбери композицию"
                currentArtist = metadata?.artist?.toString()?.takeIf { it.isNotBlank() } ?: "Локальная музыка"
                currentArtwork = metadata?.artworkUri
                    ?: tracks.firstOrNull { it.id.toString() == item?.mediaId }?.artworkUri
                isPlaying = player.isPlaying
                positionMs = player.currentPosition.coerceAtLeast(0L)
                val playerDuration = player.duration
                durationMs = if (playerDuration != C.TIME_UNSET && playerDuration > 0L) {
                    playerDuration
                } else {
                    tracks.firstOrNull { it.id.toString() == item?.mediaId }?.durationMs ?: 0L
                }
                shuffleEnabled = player.shuffleModeEnabled
                repeatMode = player.repeatMode
            }
            delay(400L)
        }
    }

    fun requestAudioAccess() {
        audioPermissionLauncher.launch(audioPermission)
    }

    fun toggleFavorite(trackId: String?) {
        val id = trackId?.toLongOrNull() ?: return
        val updated = favoriteIds.toMutableSet().apply {
            if (!add(id)) remove(id)
        }.toSet()
        favoriteIds = updated
        preferences.edit().putStringSet("favorite_ids", updated.map { it.toString() }.toSet()).apply()
    }

    fun playTrack(track: AudioTrack) {
        val player = controller ?: return
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        player.setMediaItems(tracks.map { it.toMediaItem() }, index, 0L)
        player.prepare()
        player.play()
        page = PAGE_PLAYER
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.mediaItemCount == 0 && tracks.isNotEmpty()) {
            playTrack(tracks.first())
        } else if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun stopPlayback() {
        controller?.stop()
        controller?.clearMediaItems()
        positionMs = 0L
        durationMs = 0L
    }

    fun nextTrack() {
        controller?.seekToNextMediaItem()
    }

    fun previousTrack() {
        controller?.seekToPreviousMediaItem()
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun toggleRepeat() {
        controller?.let {
            it.repeatMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AuralisColors.Background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(AuralisColors.Background)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color(0xFF8D438F).copy(alpha = 0.82f),
                    0.28f to Color(0xFF4A214E).copy(alpha = 0.63f),
                    0.60f to Color(0xFF1C1324).copy(alpha = 0.30f),
                    1f to Color.Transparent,
                    startY = 0f,
                    endY = size.height * 0.69f
                ),
                size = Size(size.width, size.height * 0.72f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFCE85D6).copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(size.width * 0.50f, size.height * 0.12f),
                    radius = size.width * 0.80f
                ),
                radius = size.width * 0.80f,
                center = Offset(size.width * 0.50f, size.height * 0.12f)
            )
        }
        GlowOrb(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 42.dp)
                .size(230.dp),
            color = Color(0xFFB35EC1),
            blur = 66.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(WindowInsets.navigationBars.asPaddingValues())
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (page) {
                    PAGE_PLAYER -> PlayerScreen(
                        title = currentTitle,
                        artist = currentArtist,
                        artwork = currentArtwork,
                        isPlaying = isPlaying,
                        isFavorite = currentTrackId?.toLongOrNull()?.let { it in favoriteIds } == true,
                        onFavorite = { toggleFavorite(currentTrackId) },
                        positionMs = positionMs,
                        durationMs = durationMs,
                        shuffleEnabled = shuffleEnabled,
                        repeatMode = repeatMode,
                        onPlayPause = ::togglePlayPause,
                        onPrevious = ::previousTrack,
                        onNext = ::nextTrack,
                        onShuffle = ::toggleShuffle,
                        onRepeat = ::toggleRepeat,
                        onSeek = { controller?.seekTo(it) },
                        onStop = ::stopPlayback,
                        onOpenQueue = { page = PAGE_LIBRARY }
                    )

                    PAGE_LIBRARY -> LibraryScreen(
                        tracks = tracks,
                        loading = loading,
                        hasPermission = hasAudioPermission,
                        currentTrackId = currentTrackId,
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { toggleFavorite(it) },
                        onPlayTrack = ::playTrack,
                        onRefresh = {
                            if (hasAudioPermission) {
                                scope.launch {
                                    loading = true
                                    tracks = AudioRepository(context).scanDevice()
                                    loading = false
                                }
                            } else requestAudioAccess()
                        },
                        onGrantAccess = ::requestAudioAccess
                    )

                    PAGE_HOME -> HomeScreen(
                        tracks = tracks,
                        currentTitle = currentTitle,
                        currentArtist = currentArtist,
                        currentArtwork = currentArtwork,
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { toggleFavorite(it) },
                        onOpenLibrary = { page = PAGE_LIBRARY },
                        onPlayTrack = ::playTrack
                    )

                    PAGE_SOUND -> SoundScreen(
                        hasNotificationPermission = hasNotificationPermission,
                        equalizerEnabled = equalizerEnabled,
                        equalizerPresetId = equalizerPresetId,
                        equalizerGains = equalizerGains,
                        hasCurrentTrack = currentTrackId != null,
                        onEqualizerEnabledChange = { equalizerEnabled = it },
                        onEqualizerPresetSelected = { preset ->
                            equalizerPresetId = preset.id
                            equalizerGains = preset.valuesDb
                        },
                        onEqualizerGainChanged = { index, value ->
                            equalizerGains = equalizerGains.toMutableList().also { it[index] = value.coerceIn(-12f, 12f) }
                            equalizerPresetId = "custom"
                        },
                        onEqualizerReset = {
                            equalizerPresetId = "flat"
                            equalizerGains = AuralisEqualizer.flatProfile
                        },
                        onRequestNotifications = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )

                    PAGE_TIMER -> SleepTimerScreen(
                        selectedMinutes = timerMinutes,
                        active = timerActive,
                        onSelectMinutes = { timerMinutes = it },
                        onStart = {
                            timerJob?.cancel()
                            timerActive = true
                            timerJob = scope.launch {
                                delay(timerMinutes * 60_000L)
                                controller?.pause()
                                timerActive = false
                            }
                        },
                        onCancel = {
                            timerJob?.cancel()
                            timerJob = null
                            timerActive = false
                        }
                    )
                }
            }

            if (page != PAGE_PLAYER && currentTrackId != null) {
                MiniPlayer(
                    title = currentTitle,
                    artist = currentArtist,
                    artworkUri = currentArtwork,
                    isPlaying = isPlaying,
                    onOpen = { page = PAGE_PLAYER },
                    onPlayPause = ::togglePlayPause,
                    onNext = ::nextTrack
                )
            }

            BottomDock(selected = page, onSelect = { page = it })
        }
    }
}

@Composable
private fun PlayerScreen(
    title: String,
    artist: String,
    artwork: Uri?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onFavorite: () -> Unit,
    positionMs: Long,
    durationMs: Long,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onSeek: (Long) -> Unit,
    onStop: () -> Unit,
    onOpenQueue: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var scrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember { mutableFloatStateOf(0f) }
    val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val shownProgress = if (scrubbing) scrubValue else progress
    val shownPosition = (shownProgress * durationMs).toLong()
    val remaining = (durationMs - shownPosition).coerceAtLeast(0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onOpenQueue) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "К медиатеке", tint = Color.White.copy(alpha = 0.92f))
            }
            Text("NOW PLAYING", color = Color.White.copy(alpha = 0.76f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Меню", tint = Color.White)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Открыть медиатеку") },
                        onClick = { menuExpanded = false; onOpenQueue() }
                    )
                    DropdownMenuItem(
                        text = { Text("Остановить воспроизведение") },
                        leadingIcon = { Icon(Icons.Rounded.Stop, contentDescription = null) },
                        onClick = { menuExpanded = false; onStop() }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AuralisColors.Text,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Light,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(artist, color = Color.White.copy(alpha = 0.70f), fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "В избранное",
                    tint = if (isFavorite) Color(0xFFFF9ECF) else Color.White.copy(alpha = 0.88f),
                    modifier = Modifier.size(27.dp)
                )
            }
        }

        VinylVisual(
            artwork = artwork,
            progress = shownProgress,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxWidth().height(326.dp).padding(top = 7.dp)
        )

        Column(Modifier.fillMaxWidth().padding(top = 3.dp)) {
            Slider(
                value = shownProgress,
                onValueChange = {
                    scrubbing = true
                    scrubValue = it
                },
                onValueChangeFinished = {
                    onSeek((scrubValue * durationMs).toLong())
                    scrubbing = false
                },
                enabled = durationMs > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = AuralisColors.PurpleBright,
                    activeTrackColor = AuralisColors.Purple,
                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                ),
                modifier = Modifier.height(29.dp)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(shownPosition), color = Color.White.copy(alpha = 0.66f), fontSize = 11.sp)
                Text("-${formatDuration(remaining)}", color = Color.White.copy(alpha = 0.66f), fontSize = 11.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 17.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onShuffle) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Перемешать", tint = if (shuffleEnabled) AuralisColors.PurpleBright else Color.White.copy(alpha = 0.75f), modifier = Modifier.size(23.dp))
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Предыдущий трек", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFE4D5FF), Color(0xFFB99AFF), Color(0xFF8D70CA))))
                    .border(1.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                    .clickable(onClick = onPlayPause),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                    tint = Color(0xFF20182C),
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Следующий трек", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onRepeat) {
                Icon(
                    imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    contentDescription = "Повтор",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) AuralisColors.PurpleBright else Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(23.dp)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Headphones, contentDescription = null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(15.dp))
            Text("  LOCAL AUDIO  ·  AURALIS PLAYER", color = Color.White.copy(alpha = 0.35f), fontSize = 9.sp, letterSpacing = 1.8.sp)
        }
    }
}

@Composable
private fun VinylVisual(
    artwork: Uri?,
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "vinyl_rotation")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotation_value"
    )

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val discSize = maxWidth.coerceAtMost(320.dp)
        Artwork(
            artworkUri = artwork,
            modifier = Modifier
                .size(112.dp)
                .offset(x = -(discSize * 0.34f))
                .graphicsLayer { alpha = 0.78f; rotationZ = -12f }
        )
        Artwork(
            artworkUri = artwork,
            modifier = Modifier
                .size(112.dp)
                .offset(x = discSize * 0.34f)
                .graphicsLayer { alpha = 0.78f; rotationZ = 12f }
        )

        Canvas(Modifier.size(discSize)) {
            val radius = size.minDimension * 0.485f
            val ringBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF41384F).copy(alpha = 0.95f),
                    Color(0xFF18151F).copy(alpha = 0.98f),
                    Color(0xFF595269).copy(alpha = 0.78f),
                    Color(0xFF16131D).copy(alpha = 0.98f)
                ),
                center = center,
                radius = radius
            )
            drawCircle(brush = ringBrush, radius = radius)
            drawCircle(color = Color.White.copy(alpha = 0.34f), radius = radius, style = Stroke(width = 1.4.dp.toPx()))
            drawCircle(color = Color(0xFFB9A4D9).copy(alpha = 0.45f), radius = radius * 0.87f, style = Stroke(width = 1.1.dp.toPx()))
            drawCircle(color = Color.White.copy(alpha = 0.12f), radius = radius * 0.74f, style = Stroke(width = 1.dp.toPx()))
            drawArc(
                color = Color(0xFF7AB7FF),
                startAngle = -90f,
                sweepAngle = progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.95f, center.y - radius * 0.95f),
                size = Size(radius * 1.9f, radius * 1.9f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            // Fine radial grooves make the center look more like a vinyl record.
            for (index in 0..5) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.025f + index * 0.005f),
                    radius = radius * (0.62f + index * 0.018f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Artwork(
            artworkUri = artwork,
            modifier = Modifier
                .size(discSize * 0.57f)
                .clip(CircleShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.36f), CircleShape)
                .graphicsLayer { rotationZ = if (isPlaying) rotation else 0f },
            iconSize = 44.dp
        )
    }
}

@Composable
private fun LibraryScreen(
    tracks: List<AudioTrack>,
    loading: Boolean,
    hasPermission: Boolean,
    currentTrackId: String?,
    favoriteIds: Set<Long>,
    onToggleFavorite: (String) -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onRefresh: () -> Unit,
    onGrantAccess: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var search by rememberSaveable { mutableStateOf("") }
    var selectedFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedAlbum by rememberSaveable { mutableStateOf<String?>(null) }

    val visibleTracks = remember(tracks, search, selectedFolder, selectedAlbum, selectedTab, favoriteIds) {
        tracks.filter { track ->
            val matchesSearch = search.isBlank() ||
                track.title.contains(search, ignoreCase = true) ||
                track.artist.contains(search, ignoreCase = true) ||
                track.album.contains(search, ignoreCase = true) ||
                track.relativePath.contains(search, ignoreCase = true)
            val matchesFolder = selectedFolder == null || track.relativePath == selectedFolder
            val matchesAlbum = selectedAlbum == null || "${track.album}|${track.artist}" == selectedAlbum
            val matchesFavorite = selectedTab != 3 || track.id in favoriteIds
            matchesSearch && matchesFolder && matchesAlbum && matchesFavorite
        }
    }
    val folders = remember(tracks) { tracks.groupBy { it.relativePath } }
    val albums = remember(tracks) { tracks.groupBy { "${it.album}|${it.artist}" } }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Твоя медиатека", color = AuralisColors.Text, fontSize = 27.sp, fontWeight = FontWeight.Light)
                Text("${tracks.size} композиций на устройстве", color = AuralisColors.Muted, fontSize = 12.sp)
            }
            IconButton(onClick = onRefresh) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(19.dp), strokeWidth = 2.dp, color = AuralisColors.Purple)
                else Icon(Icons.Rounded.Refresh, contentDescription = "Обновить библиотеку", tint = AuralisColors.Text)
            }
        }

        GlassSurface(modifier = Modifier.fillMaxWidth().height(48.dp), cornerRadius = 18.dp) {
            Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = AuralisColors.Muted, modifier = Modifier.size(20.dp))
                BasicTextField(
                    value = search,
                    onValueChange = { search = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = AuralisColors.Text, fontSize = 14.sp),
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    decorationBox = { inner ->
                        Box {
                            if (search.isEmpty()) Text("Поиск треков, альбомов, папок", color = AuralisColors.Muted, fontSize = 13.sp)
                            inner()
                        }
                    }
                )
                if (search.isNotEmpty()) {
                    IconButton(onClick = { search = "" }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Очистить поиск", tint = AuralisColors.Muted, modifier = Modifier.size(17.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LibraryTab("Треки", selectedTab == 0) { selectedTab = 0; selectedFolder = null; selectedAlbum = null }
            LibraryTab("Папки", selectedTab == 1) { selectedTab = 1; selectedFolder = null; selectedAlbum = null }
            LibraryTab("Альбомы", selectedTab == 2) { selectedTab = 2; selectedFolder = null; selectedAlbum = null }
            LibraryTab("Избранное", selectedTab == 3) { selectedTab = 3; selectedFolder = null; selectedAlbum = null }
        }

        if (!hasPermission) {
            EmptyLibrary(onGrantAccess, "Нужен доступ к музыке")
        } else if (tracks.isEmpty()) {
            GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 15.dp), cornerRadius = 24.dp) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Album, contentDescription = null, tint = AuralisColors.Purple, modifier = Modifier.size(40.dp))
                    Text(if (loading) "Сканируем музыку…" else "Музыка не найдена", color = AuralisColors.Text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                    Text("Добавь аудиофайлы в папку Music или Download и обнови медиатеку.", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    Button(onClick = onRefresh, modifier = Modifier.padding(top = 12.dp)) { Text("Повторить сканирование") }
                }
            }
        } else {
            when (selectedTab) {
                0, 3 -> {
                    if (visibleTracks.isEmpty()) {
                        Text("Ничего не найдено", color = AuralisColors.Muted, modifier = Modifier.padding(top = 24.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (selectedFolder != null || selectedAlbum != null) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { selectedFolder = null; selectedAlbum = null }.padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.ArrowBack, contentDescription = null, tint = AuralisColors.Purple)
                                        Text("  Все треки", color = AuralisColors.PurpleBright, fontSize = 13.sp)
                                    }
                                }
                            }
                            items(visibleTracks, key = { it.id }) { track ->
                                TrackRow(
                                    track = track,
                                    isCurrent = currentTrackId == track.id.toString(),
                                    onClick = { onPlayTrack(track) },
                                    isFavorite = track.id in favoriteIds,
                                    onFavoriteClick = { onToggleFavorite(track.id.toString()) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(folders.entries.toList(), key = { it.key }) { entry ->
                            GlassSurface(modifier = Modifier.fillMaxWidth().clickable { selectedFolder = entry.key; selectedAlbum = null; selectedTab = 0 }, cornerRadius = 18.dp) {
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(43.dp).clip(RoundedCornerShape(14.dp)).background(Color(0x3DB99AFF)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.FolderOpen, contentDescription = null, tint = AuralisColors.PurpleBright)
                                    }
                                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                        Text(entry.key.trimEnd('/').substringAfterLast('/'), color = AuralisColors.Text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(entry.key, color = AuralisColors.Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text("${entry.value.size}", color = AuralisColors.Muted, fontSize = 12.sp)
                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AuralisColors.Muted)
                                }
                            }
                        }
                    }
                }
                else -> {
                    val albumRows = albums.entries.toList().chunked(2)
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 18.dp)) {
                        items(albumRows) { pair ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                pair.forEach { entry ->
                                    val first = entry.value.first()
                                    Box(Modifier.weight(1f)) {
                                        AlbumTile(
                                            title = first.album,
                                            artist = first.artist,
                                            artworkUri = first.artworkUri,
                                            onClick = { selectedAlbum = entry.key; selectedFolder = null; selectedTab = 0 }
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) Color(0x55B99AFF) else Color(0x1CFFFFFF))
            .border(1.dp, if (selected) Color(0x88D5C3FF) else Color.White.copy(alpha = 0.09f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else AuralisColors.Muted, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun HomeScreen(
    tracks: List<AudioTrack>,
    currentTitle: String,
    currentArtist: String,
    currentArtwork: Uri?,
    favoriteIds: Set<Long>,
    onToggleFavorite: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    onPlayTrack: (AudioTrack) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AURALIS PLAYER", color = Color.White.copy(alpha = 0.62f), fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Text("Твоя музыка.\nТвоё пространство.", color = AuralisColors.Text, fontSize = 29.sp, lineHeight = 34.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 10.dp))
            }
            MiniEqualizerBars(Modifier.padding(end = 7.dp))
        }

        GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 22.dp), cornerRadius = 27.dp) {
            Row(Modifier.fillMaxWidth().clickable(onClick = onOpenLibrary).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Artwork(currentArtwork, Modifier.size(58.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Продолжить", color = AuralisColors.Muted, fontSize = 11.sp)
                    Text(currentTitle, color = AuralisColors.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(currentArtist, color = AuralisColors.Muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AuralisColors.Purple)
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 25.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Недавно добавленные", color = AuralisColors.Text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("ВСЯ МУЗЫКА", color = AuralisColors.PurpleBright, fontSize = 9.sp, letterSpacing = 1.5.sp, modifier = Modifier.clickable(onClick = onOpenLibrary))
        }
        if (tracks.isEmpty()) {
            GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), cornerRadius = 22.dp) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Пока нет музыки", color = AuralisColors.Text, fontWeight = FontWeight.SemiBold)
                    Text("Открой медиатеку, чтобы найти аудио на устройстве.", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(1.dp), contentPadding = PaddingValues(bottom = 15.dp)) {
                items(tracks.take(6), key = { it.id }) { track ->
                    TrackRow(
                        track = track,
                        isCurrent = false,
                        onClick = { onPlayTrack(track) },
                        isFavorite = track.id in favoriteIds,
                        onFavoriteClick = { onToggleFavorite(track.id.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SoundScreen(
    hasNotificationPermission: Boolean,
    equalizerEnabled: Boolean,
    equalizerPresetId: String,
    equalizerGains: List<Float>,
    hasCurrentTrack: Boolean,
    onEqualizerEnabledChange: (Boolean) -> Unit,
    onEqualizerPresetSelected: (com.daimonxx.auralis.playback.EqualizerPreset) -> Unit,
    onEqualizerGainChanged: (Int, Float) -> Unit,
    onEqualizerReset: () -> Unit,
    onRequestNotifications: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 18.dp)
    ) {
        Text("Звук и система", color = AuralisColors.Text, fontSize = 27.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 18.dp))
        Text("Настрой звучание Auralis под себя", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 17.dp))

        EqualizerPanel(
            enabled = equalizerEnabled,
            selectedPresetId = equalizerPresetId,
            gainsDb = equalizerGains,
            onEnabledChange = onEqualizerEnabledChange,
            onPresetSelected = onEqualizerPresetSelected,
            onGainChanged = onEqualizerGainChanged,
            onReset = onEqualizerReset,
            hasCurrentTrack = hasCurrentTrack
        )

        GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), cornerRadius = 22.dp) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Headphones, contentDescription = null, tint = AuralisColors.PurpleBright, modifier = Modifier.size(27.dp))
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Bluetooth и наушники", color = AuralisColors.Text, fontWeight = FontWeight.SemiBold)
                    Text("Кнопки гарнитуры управляют текущим треком через MediaSession.", color = AuralisColors.Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp))
                }
                Icon(Icons.Rounded.CheckCircle, contentDescription = "Поддерживается", tint = Color(0xFF82D8B2))
            }
        }
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 11.dp), cornerRadius = 22.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.NotificationsNone, contentDescription = null, tint = AuralisColors.PurpleBright, modifier = Modifier.size(25.dp))
                    Text("Системные уведомления", color = AuralisColors.Text, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp))
                }
                Text(
                    if (hasNotificationPermission) "Разрешение выдано. Android сможет показывать медиаконтролы в панели уведомлений." else "Разреши уведомления, чтобы системная панель показывала текущий трек и элементы управления.",
                    color = AuralisColors.Muted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= 33) {
                    Button(onClick = onRequestNotifications) { Text("Разрешить уведомления") }
                } else {
                    Text("Уведомления настроены", color = Color(0xFF82D8B2), fontSize = 12.sp)
                }
            }
        }
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(top = 11.dp), cornerRadius = 22.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Аудиоформаты", color = AuralisColors.Text, fontWeight = FontWeight.SemiBold)
                Text("Воспроизведение зависит от декодеров Android. Обычно поддерживаются MP3, AAC/M4A, FLAC, WAV, OGG и Opus.", color = AuralisColors.Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}

@Composable
private fun SleepTimerScreen(
    selectedMinutes: Int,
    active: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onStart: () -> Unit,
    onCancel: () -> Unit
) {
    val options = listOf(15, 30, 45, 60, 90)
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Text("Таймер сна", color = AuralisColors.Text, fontSize = 27.sp, fontWeight = FontWeight.Light, modifier = Modifier.padding(top = 18.dp))
        Text("Auralis остановит музыку автоматически", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 25.dp))
        GlassSurface(modifier = Modifier.fillMaxWidth(), cornerRadius = 26.dp) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = AuralisColors.PurpleBright, modifier = Modifier.size(54.dp))
                Text(if (active) "Таймер запущен" else "Выбери время", color = AuralisColors.Text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                Text(if (active) "Воспроизведение остановится по окончании отсчёта." else "Через сколько минут остановить воспроизведение?", color = AuralisColors.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    options.forEach { minutes ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .background(if (selectedMinutes == minutes) Color(0x66B99AFF) else Color(0x1EFFFFFF))
                                .border(1.dp, if (selectedMinutes == minutes) Color(0x99D4C2FF) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(15.dp))
                                .clickable(enabled = !active) { onSelectMinutes(minutes) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$minutes", color = if (selectedMinutes == minutes) Color.White else AuralisColors.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Button(onClick = if (active) onCancel else onStart, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Text(if (active) "Отменить таймер" else "Запустить таймер")
                }
            }
        }
    }
}
