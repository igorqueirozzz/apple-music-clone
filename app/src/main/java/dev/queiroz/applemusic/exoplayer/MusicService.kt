package dev.queiroz.applemusic.exoplayer

import android.content.Intent
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    private val progressListeners = mutableListOf<MusicServiceListener>()
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
        setupExoPlayer()
        mediaSession = MediaSession.Builder(this, exoPlayer).build()
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
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                when (isPlaying) {
                    true -> progressListeners.forEach { it.onPlayMusic() }
                    false -> progressListeners.forEach { it.onPauseMusic() }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    PlaybackState.STATE_PLAYING -> {
                        val totalPosition = exoPlayer.duration.toInt()
                        progressListeners.forEach { listener ->
                            listener.onMusicProgressUpdate(0, totalPosition)
                            listener.onPlayMusic()
                        }
                        progressUpdateHandler = Handler(Looper.getMainLooper())
                        progressUpdateHandler?.post(updateProgressAction)
                    }

                    PlaybackState.STATE_STOPPED, PlaybackState.STATE_PAUSED, PlaybackState.STATE_ERROR, PlaybackState.ACTION_PAUSE.toInt() -> {
                        progressListeners.forEach { listener ->
                            listener.onPauseMusic()
                        }
                    }

                    else -> {
                        progressUpdateHandler = null
                    }
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

    fun addProgressListener(listener: MusicServiceListener) {
        progressListeners.add(listener)
    }

    fun removeProgressListener(listener: MusicServiceListener) {
        progressListeners.remove(listener)
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return MusicServiceBinder()
    }

    fun getPlayer(): ExoPlayer = exoPlayer

    fun play() = exoPlayer.play()
    fun pause() = exoPlayer.pause()
    fun nextMusic() = exoPlayer.seekToNext()
    fun previousMusic() = exoPlayer.seekToPrevious()

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }
}