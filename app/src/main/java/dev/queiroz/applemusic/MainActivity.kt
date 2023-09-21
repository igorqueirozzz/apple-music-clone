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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.databinding.ActivityMainBinding
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.ui.home.HomeFragment
import dev.queiroz.applemusic.ui.search.SearchFragment
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import dev.queiroz.applemusic.utils.MediaItemUtils

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

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()

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
        setupObservers()
        setupListeners()
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicServiceConnection)
        musicService?.onDestroy()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuListenNow -> setCurrentFragment(fragment = homeFragment)
                R.id.menuSearch -> setCurrentFragment(fragment = searchFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_host_fragment, fragment)
            commit()
        }

    private fun setupObservers() {
        val floatingMiniPlayerImageView = findViewById<ImageView>(R.id.floatingMiniPlayerImageView)
        val floatingMiniPlayerSongName = findViewById<TextView>(R.id.floatingMiniPlayerSongName)
        val floatingMiniPlayerPlayButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerPlayButton)

        with(appleMusicViewModel) {
            currentSong.observe(this@MainActivity) {
                musicService?.getPlayer()
                    ?.setMediaItem(MediaItemUtils.mediaItemFromSong(it))

                //Floating miniplayer
                Glide
                    .with(this@MainActivity)
                    .load(it.albumImageUrl)
                    .into(floatingMiniPlayerImageView)
                floatingMiniPlayerSongName.text = it.trackName

            }

            isSongPlaying.observe(this@MainActivity) { isSongPlaying ->
                floatingMiniPlayerPlayButton.setImageResource(if (isSongPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                floatingMiniPlayerPlayButton.setOnClickListener {
                    if (isSongPlaying) {
                        musicService?.pause()
                    } else {
                        musicService?.play()
                    }
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
                appleMusicViewModel.playMusic(it!!)
            }
        }

        floatingMiniPlayerImageView.setOnClickListener {
            navController?.navigate(R.id.playerFragment)
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