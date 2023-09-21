package dev.queiroz.applemusic.utils

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dev.queiroz.applemusic.model.Song

object MediaItemUtils {
    fun mediaItemFromSong(song: Song): MediaItem {
        return MediaItem
            .Builder()
            .setUri(song.songUrl)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setAlbumArtist(song.collectionName)
                    .setArtist(song.artistName)
                    .setArtworkUri(Uri.parse(song.albumImageUrl))
                    .setSubtitle(song.artistName)
                    .setTitle(song.trackName)
                    .build()
            ).build()
    }
}