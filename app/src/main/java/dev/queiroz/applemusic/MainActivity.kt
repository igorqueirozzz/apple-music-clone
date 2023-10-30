package dev.queiroz.applemusic

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.databinding.ActivityMainBinding
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import dev.queiroz.applemusic.ui.viewmodel.MusicState
import dev.queiroz.applemusic.utils.MediaItemUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val appleMusicViewModel: AppleMusicViewModel by viewModels()
    private var musicService: MusicService? = null

    private val musicServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            val musicServiceBinder = binder as MusicService.MusicServiceBinder
            musicService = musicServiceBinder.getService()
            musicService?.addProgressListener(appleMusicViewModel)
        }

        override fun onServiceDisconnected(p0: ComponentName?) = Unit
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        Intent(this, MusicService::class.java).also {
            it.action = MusicService.Action.START_SERVICE.toString()
            startService(it)
        }
        val serviceConnectionIntent = Intent(this, MusicService::class.java)
        bindService(serviceConnectionIntent, musicServiceConnection, 0)

        setContentView(binding.root)
        setupBottomNavigation()
        lifecycleScope.launch {
            setupObservers()
        }
        setupListeners()
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicServiceConnection)
        musicService?.onDestroy()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuListenNow -> navController.navigate(R.id.homeFragment)
                R.id.menuSearch -> navController.navigate(R.id.searchFragment)
            }
            true
        }
    }

    private suspend fun setupObservers() {
        val floatingMiniPlayerImageView = findViewById<ImageView>(R.id.floatingMiniPlayerImageView)
        val floatingMiniPlayerSongName = findViewById<TextView>(R.id.floatingMiniPlayerSongName)
        val floatingMiniPlayerPlayButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerPlayButton)
        val floatingMiniPlayerLoadingIndicator =
            findViewById<LinearProgressIndicator>(R.id.floatingMiniPlayerLoadingIndicator)

        appleMusicViewModel.currentSong.collectLatest { musicState ->
            when (musicState) {
                is MusicState.Selected -> {
                    val song = musicState.song!!
                    Glide
                        .with(this@MainActivity)
                        .load(song.albumImageUrl)
                        .into(floatingMiniPlayerImageView)
                    floatingMiniPlayerSongName.text = song.trackName
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_play)
                    floatingMiniPlayerPlayButton.setOnClickListener { appleMusicViewModel.playMusic(song = song)}
                }

                is MusicState.Loading -> {
                    floatingMiniPlayerLoadingIndicator.show()
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.loading),
                        Toast.LENGTH_SHORT
                    ).show()
                    val song = musicState.song!!
                    Glide
                        .with(this@MainActivity)
                        .load(song.albumImageUrl)
                        .into(floatingMiniPlayerImageView)
                    floatingMiniPlayerSongName.text = getString(R.string.loading)
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_pause)
                    floatingMiniPlayerPlayButton.setOnClickListener { null }
                    musicService?.getPlayer()?.clearMediaItems()
                    musicService?.getPlayer()
                        ?.setMediaItem(MediaItemUtils.mediaItemFromSong(song = song))
                    musicService?.getPlayer()?.prepare()
                }

                is MusicState.Loaded -> {
                    floatingMiniPlayerLoadingIndicator.hide()
                    floatingMiniPlayerPlayButton.setOnClickListener { musicService?.pause() }
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_pause)
                }

                is MusicState.Playing -> {
                    floatingMiniPlayerLoadingIndicator.hide()
                    floatingMiniPlayerPlayButton.setOnClickListener { musicService?.pause() }
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_pause)
                    floatingMiniPlayerSongName.text = musicState.song!!.trackName
                }

                is MusicState.Stopped -> {
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_play)
                    floatingMiniPlayerSongName.text = musicState.song!!.trackName
                    floatingMiniPlayerLoadingIndicator.hide()
                    floatingMiniPlayerPlayButton.setOnClickListener {
                        appleMusicViewModel.playMusic(
                            musicState.song!!
                        )
                    }
                }

                is MusicState.OnPause -> {
                    musicService?.pause()
                }

                is MusicState.OnUnpause -> {
                    musicService?.play()
                }

                is MusicState.Paused -> {
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_play)
                    floatingMiniPlayerSongName.text = musicState.song!!.trackName
                    floatingMiniPlayerLoadingIndicator.hide()
                    floatingMiniPlayerPlayButton.setOnClickListener { musicService?.play() }
                }

                is MusicState.ErrorOnLoad -> {
                    val song = musicState.song!!
                    Toast.makeText(this@MainActivity, musicState.errorMessage, Toast.LENGTH_LONG)
                        .show()
                    Glide
                        .with(this@MainActivity)
                        .load(song.albumImageUrl)
                        .into(floatingMiniPlayerImageView)
                    floatingMiniPlayerLoadingIndicator.hide()
                    floatingMiniPlayerSongName.text = song.trackName
                    floatingMiniPlayerPlayButton.setImageResource(R.drawable.ic_play)
                    floatingMiniPlayerPlayButton.setOnClickListener { appleMusicViewModel.playMusic(song = song)}
                }

            }
        }

    }

    private fun setupListeners() {
        val floatingMiniPlayerBackButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerBackButton)
        val floatingMiniPlayerNextButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerNextButton)
        val floatingMiniPlayerPlayButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerPlayButton)
        val floatingMiniPlayerImageView = findViewById<ImageView>(R.id.floatingMiniPlayerImageView)

        floatingMiniPlayerBackButton.setOnClickListener {
            musicService?.previousMusic()
        }

        floatingMiniPlayerNextButton.setOnClickListener {
            musicService?.nextMusic()
        }

        floatingMiniPlayerPlayButton.setOnClickListener {
            appleMusicViewModel.currentSong.value.let {
                appleMusicViewModel.playMusic(it.song!!)
            }
        }

        floatingMiniPlayerImageView.setOnClickListener {
            navController.navigate(R.id.playerFragment)
        }

    }

    fun hideBottomComponents() {
        with(binding) {
            bottomNavigation.visibility = View.GONE
            floatingMiniPlayer.visibility = View.GONE
        }
    }

    fun showBottomComponents() {
        with(binding) {
            bottomNavigation.visibility = View.VISIBLE
            floatingMiniPlayer.visibility = View.VISIBLE
        }
    }
}