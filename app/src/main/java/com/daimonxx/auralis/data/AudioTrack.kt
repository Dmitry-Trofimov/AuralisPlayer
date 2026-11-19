package com.daimonxx.auralis.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/** A local audio file indexed by Android MediaStore. */
data class AudioTrack(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumId: Long,
    val relativePath: String
) {
    val artworkUri: Uri?
        get() = if (albumId > 0L) {
            Uri.parse("content://media/external/audio/albumart/$albumId")
        } else null

    fun toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .apply { if (artworkUri != null) setArtworkUri(artworkUri) }
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }
}
