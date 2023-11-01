package dev.queiroz.applemusic.exoplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.session.PlaybackState
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.R
import dev.queiroz.applemusic.constants.NotificationConstants
import dev.queiroz.applemusic.ui.viewmodel.MusicState
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    private val listeners = mutableListOf<MusicServiceListener>()
    private var progressUpdateHandler: Handler? = null
    private val updateProgressAction = object : Runnable {
        override fun run() {
            if (exoPlayer.isPlaying) {
                val currentPosition =
                    if (exoPlayer.currentPosition.toInt() < 0) 0 else exoPlayer.currentPosition.toInt()
                val totalDuration = exoPlayer.duration.toInt()
                listeners.forEach { listener ->
                    listener.onStateChanged(
                        MusicState.Playing(
                            songPosition = Pair(
                                currentPosition,
                                totalDuration
                            )
                        )
                    )
                }

            }
            progressUpdateHandler?.postDelayed(this, 10)
        }
    }

    private lateinit var notification: Notification

    private var previousMusicAction: Notification.Action? = null
    private var playMusicAction: Notification.Action? = null
    private var pauseMusicAction: Notification.Action? = null
    private var playPauseMusicAction: Notification.Action? = null
    private var nextMusicAction: Notification.Action? = null
    private var albumBitmap: Bitmap? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Action.START_SERVICE.toString() -> startSelf()
            Action.STOP_SERVICE.toString() -> stopSelf()
            Action.PLAY.toString() -> play()
            Action.PREVIOUS.toString() -> previousMusic()
            Action.NEXT.toString() -> nextMusic()
            Action.PAUSE.toString() -> pause()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startSelf() {
        setupExoPlayer()
        setupNotificationActions()
        notification = Notification
            .Builder(this, NotificationConstants.notificationChannelId)
            .setSmallIcon(R.drawable.ic_play)
            .setActions(
                previousMusicAction,
                playMusicAction,
                nextMusicAction
            )
            .setStyle(Notification.MediaStyle())
            .build()

        startForeground(1, notification)
    }

    private fun setupExoPlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                val duration = exoPlayer.duration.toInt()
                val position =
                    if (exoPlayer.currentPosition < 0) 0 else exoPlayer.currentPosition.toInt()
                playPauseMusicAction = when (isPlaying) {
                    true -> {
                        listeners.forEach {
                            it.onStateChanged(
                                MusicState.Playing(
                                    songPosition = Pair(position, duration)
                                )
                            )
                        }
                        pauseMusicAction
                    }

                    false -> {
                        listeners.forEach {
                            it.onStateChanged(
                                MusicState.Paused(
                                    songPosition = Pair(
                                        position,
                                        duration
                                    )
                                )
                            )
                        }
                        playMusicAction
                    }
                }
                updateNotification()
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                listeners.forEach { listener ->
                    listener.onStateChanged(
                        MusicState.ErrorOnLoad(
                            errorMessage = getString(
                                R.string.can_t_load_song_check_your_connection_and_try_again
                            )
                        )
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    PlaybackState.STATE_PLAYING -> {
                        val finalPosition = exoPlayer.duration.toInt()
                        val position =
                            if (exoPlayer.currentPosition < 0) 0 else exoPlayer.currentPosition.toInt()
                        listeners.forEach { listener ->
                            listener.onStateChanged(
                                MusicState.Playing(
                                    songPosition = Pair(
                                        position,
                                        finalPosition
                                    )
                                )
                            )
                        }
                        progressUpdateHandler = Handler(Looper.getMainLooper())
                        progressUpdateHandler?.post(updateProgressAction)

                    }

                    PlaybackState.STATE_STOPPED -> listeners.forEach { listener ->
                        listener.onStateChanged(
                            MusicState.Stopped()
                        )
                    }

                    PlaybackState.STATE_PAUSED -> listeners.forEach { listener ->
                        val currentPosition =
                            if (exoPlayer.currentPosition < 0) 0 else exoPlayer.currentPosition.toInt()
                        listener.onStateChanged(
                            MusicState.Paused(
                                songPosition = Pair(
                                    currentPosition,
                                    exoPlayer.duration.toInt()
                                )
                            )
                        )
                    }

                    PlaybackState.STATE_ERROR -> {
                        listeners.forEach { listener ->
                            listener.onStateChanged(MusicState.ErrorOnLoad(errorMessage = exoPlayer.playerError.toString()))
                        }
                    }

                    PlaybackState.STATE_BUFFERING, PlaybackState.STATE_CONNECTING -> listeners.forEach { listener ->
                        listener.onStateChanged(
                            MusicState.Loading()
                        )
                    }


                }
                updateNotification()
                super.onPlaybackStateChanged(playbackState)
            }
        })

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun setupNotificationActions() {
        previousMusicAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_rewind),
            getString(R.string.previous),
            PendingIntent.getService(
                this,
                0,
                Intent(this, MusicService::class.java).also {
                    it.action = Action.PREVIOUS.toString()
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

        playMusicAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_play),
            getString(R.string.play),
            PendingIntent.getService(
                this,
                0,
                Intent(this, MusicService::class.java).also {
                    it.action = Action.PLAY.toString()
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

        nextMusicAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_forward),
            getString(R.string.next),
            PendingIntent.getService(
                this,
                0,
                Intent(this, MusicService::class.java).also {
                    it.action = Action.NEXT.toString()
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()

        pauseMusicAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_pause),
            getString(R.string.pause),
            PendingIntent.getService(
                this,
                0,
                Intent(this, MusicService::class.java).also {
                    it.action = Action.PAUSE.toString()
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        ).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        listeners.forEach { removeProgressListener(it) }
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
    }

    fun addProgressListener(listener: MusicServiceListener) {
        listeners.add(listener)
    }

    private fun removeProgressListener(listener: MusicServiceListener) {
        listeners.remove(listener)
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return MusicServiceBinder()
    }

    fun getPlayer(): ExoPlayer = exoPlayer

    private fun getArtistName() = exoPlayer.mediaMetadata.artist
    private fun getSongName() = exoPlayer.mediaMetadata.title
    private fun getArtworkUri() = exoPlayer.mediaMetadata.artworkUri

    fun play() = exoPlayer.play()
    fun pause() = exoPlayer.pause()
    fun nextMusic() = exoPlayer.seekToNext()
    fun previousMusic() = exoPlayer.seekToPrevious()

    private fun updateNotification(shouldUpdateBitmap: Boolean = true) {
        if (shouldUpdateBitmap) {
            Glide.with(this).asBitmap().load(getArtworkUri()).into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    albumBitmap = resource
                    updateNotification(shouldUpdateBitmap = false)
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
        }
        val notificationBackgroundColor =
            albumBitmap?.let { Palette.from(it).generate().vibrantSwatch?.rgb }
                ?: getColor(R.color.primaryColorLightTheme)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val updatedNotification = Notification
            .Builder(this, NotificationConstants.notificationChannelId)
            .setSmallIcon(R.drawable.ic_play)
            .setLargeIcon(albumBitmap)
            .setContentText(getSongName())
            .setContentTitle(getArtistName())
            .setColor(notificationBackgroundColor)
            .setColorized(true)
            .setActions(
                previousMusicAction,
                if (exoPlayer.isPlaying) pauseMusicAction else playPauseMusicAction,
                nextMusicAction
            )
            .setStyle(Notification.MediaStyle())
            .setOnlyAlertOnce(true)


        notificationManager.notify(1, updatedNotification.build())

    }

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }
    }

    enum class Action {
        START_SERVICE,
        STOP_SERVICE,
        PLAY,
        PREVIOUS,
        NEXT,
        PAUSE
    }
}