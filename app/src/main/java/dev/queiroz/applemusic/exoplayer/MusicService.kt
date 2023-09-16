package dev.queiroz.applemusic.exoplayer

import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dev.queiroz.applemusic.model.Song


class MusicService : MediaSessionService() {
    private lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null
    private val progressListeners = mutableListOf<MusicProgressListener>()
    private var progressUpdateHandler: Handler? = null
    private val updateProgressAction = object : Runnable {
        override fun run() {
            if (exoPlayer.isPlaying) {
                val currentPosition = exoPlayer.currentPosition.toInt()
                val totalDuration = exoPlayer.duration.toInt()
                progressListeners.forEach { listener ->
                    listener.onMusicProgressUpdate(currentPosition, totalDuration)
                }
            }
            progressUpdateHandler?.postDelayed(this, 10)
        }
    }


    override fun onCreate() {
        super.onCreate()
        val song = Song(
            artistName = "Michael Calfan",
            collectionName = "Tomorrowland 2016: The Elixir of Life",
            trackName = "Brothers",
            songUrl = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview125/v4/1d/99/de/1d99dea1-4029-30bb-3a06-f714a0916538/mzaf_5810976704725283580.plus.aac.p.m4a",
            albumImageUrl = "https://is4-ssl.mzstatic.com/image/thumb/Music18/v4/11/0e/3d/110e3d18-d05e-60f6-f15b-c0775fff0b58/5411530807338.png/500x500bb.jpg"
        )
        val mediaItem = MediaItem.Builder().apply {
            setUri(song.songUrl)
            setMediaMetadata(MediaMetadata.Builder().apply {
                setAlbumArtist(song.collectionName)
                setArtist(song.artistName)
                setTitle(song.trackName)
                setArtworkUri(Uri.parse(song.albumImageUrl))
            }.build())
        }.build()

        setupExoPlayer()
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
//        exoPlayer.setMediaItem(mediaItem)
//        exoPlayer.play()
    }

    private fun setupExoPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer
            .Builder(this).setAudioAttributes(audioAttributes, false)
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == PlaybackState.STATE_PLAYING) {
                    val totalPosition = exoPlayer.duration.toInt()
                    progressListeners.forEach { listener ->
                        listener.onMusicProgressUpdate(0, totalPosition)
                    }
                    progressUpdateHandler = Handler(Looper.getMainLooper())
                    progressUpdateHandler?.post(updateProgressAction)
                } else {
                    progressUpdateHandler = null
                }
                super.onPlaybackStateChanged(playbackState)
            }
        })

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        progressListeners.forEach { removeProgressListener(it) }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    fun addProgressListener(listener: MusicProgressListener) {
        progressListeners.add(listener)
    }

    fun removeProgressListener(listener: MusicProgressListener) {
        progressListeners.remove(listener)
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return MusicServiceBinder()
    }

    fun getPlayer(): ExoPlayer = exoPlayer

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }
}