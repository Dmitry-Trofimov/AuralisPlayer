package com.daimonxx.auralis.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daimonxx.auralis.ui.theme.AuralisColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Small process-local cache for album art; capped to a fraction of the app heap. */
private object ArtworkCache {
    private val maxSizeKb = (Runtime.getRuntime().maxMemory() / 1024L / 16L)
        .toInt()
        .coerceAtLeast(1024)

    private val cache = object : LruCache<String, Bitmap>(maxSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) { cache.put(key, bitmap) }
}

private fun decodeArtwork(context: Context, uri: Uri): Bitmap? {
    val key = uri.toString()
    ArtworkCache.get(key)?.let { return it }

    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    // Album art is displayed as a thumbnail; avoid decoding multi-megapixel images.
    val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
    var sampleSize = 1
    while (longestSide / sampleSize > 720) sampleSize *= 2

    val bitmap = resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(
            stream,
            null,
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
        )
    }
    return bitmap?.also { ArtworkCache.put(key, it) }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(24.dp, shape, clip = false, ambientColor = Color(0x663D2A61), spotColor = Color(0x663D2A61))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x4DF0DFFF),
                        Color(0x251B1728),
                        Color(0x421B1429)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color(0x66B99AFF),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                ),
                shape
            )
    ) {
        content()
    }
}

@Composable
fun GlowOrb(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF9C56B7),
    blur: Dp = 45.dp
) {
    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.62f), color.copy(alpha = 0.16f), Color.Transparent)
                ),
                CircleShape
            )
            .blur(blur)
    )
}

@Composable
fun Artwork(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 34.dp
) {
    val appContext = LocalContext.current.applicationContext
    val bitmap by produceState<Bitmap?>(null, artworkUri, appContext) {
        value = artworkUri?.let { uri ->
            withContext(Dispatchers.IO) {
                runCatching { decodeArtwork(appContext, uri) }.getOrNull()
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFE6A6E7), Color(0xFF8766BD), Color(0xFF263D71))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Обложка альбома",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.size(iconSize)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "AURALIS",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun MiniEqualizerBars(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf(9.dp, 18.dp, 12.dp, 23.dp, 14.dp, 8.dp).forEachIndexed { index, height ->
            Box(
                Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index % 2 == 0) AuralisColors.Purple else AuralisColors.Blue)
            )
        }
    }
}
