package com.daimonxx.auralis.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRepository(private val context: Context) {

    suspend fun scanDevice(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val collection = if (android.os.Build.VERSION.SDK_INT >= 29) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            projection += MediaStore.MediaColumns.RELATIVE_PATH
        } else {
            @Suppress("DEPRECATION")
            projection += MediaStore.MediaColumns.DATA
        }

        // MediaStore.Audio.Media already restricts the query to audio files.
        // Keep recordings and other valid local audio, not only rows flagged as music.
        val selection = "${MediaStore.Audio.Media.DURATION} > 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        val result = mutableListOf<AudioTrack>()

        resolver.query(
            collection,
            projection.toTypedArray(),
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val pathColumn = if (android.os.Build.VERSION.SDK_INT >= 29) {
                cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            }

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val duration = cursor.getLong(durationColumn)
                if (duration <= 0L) continue

                val title = cursor.getString(titleColumn)
                    ?.takeIf { it.isNotBlank() }
                    ?: cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    ?: "Без названия"
                val artist = cursor.getString(artistColumn)
                    ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                    ?: "Неизвестный исполнитель"
                val album = cursor.getString(albumColumn)
                    ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                    ?: "Без альбома"
                val rawPath = if (pathColumn >= 0) cursor.getString(pathColumn).orEmpty() else ""
                val path = if (android.os.Build.VERSION.SDK_INT >= 29) rawPath else File(rawPath).parent.orEmpty()
                val albumId = cursor.getLong(albumIdColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)

                result += AudioTrack(
                    id = id,
                    uri = contentUri,
                    title = title,
                    artist = artist,
                    album = album,
                    durationMs = duration,
                    albumId = albumId,
                    relativePath = path.ifBlank { "Неизвестная папка" }
                )
            }
        }

        result
    }
}
