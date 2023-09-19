package dev.queiroz.applemusic

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import dev.queiroz.applemusic.databinding.ActivityMainBinding
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.ui.home.HomeFragment
import dev.queiroz.applemusic.ui.search.SearchFragment
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var mediaController: ListenableFuture<MediaController>
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

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceConnectionIntent = Intent(this, MusicService::class.java)
        bindService(serviceConnectionIntent, musicServiceConnection, 0)
        setContentView(binding.root)
        setupBottomNavigation()
        setupObservers()
        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(musicServiceConnection)
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
                    ?.setMediaItem(MediaItem.Builder().setUri(it.songUrl).build())

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
                    if (isSongPlaying){
                        musicService?.pause()
                    }else{
                        musicService?.play()
                    }
                }
            }
        }
    }

    private fun setupListeners(){
        val floatingMiniPlayerBackButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerBackButton)
        val floatingMiniPlayerNextButton =
            findViewById<ImageView>(R.id.floatingMiniPlayerNextButton)

        val floatingMiniPlayerPlayButton = findViewById<ImageView>(R.id.floatingMiniPlayerPlayButton)

        floatingMiniPlayerBackButton.setOnClickListener {
            musicService?.previousMusic()
        }

        floatingMiniPlayerNextButton.setOnClickListener {
            musicService?.nextMusic()
        }

        floatingMiniPlayerPlayButton.setOnClickListener {
           appleMusicViewModel.currentSong.value.let {
               appleMusicViewModel.playMusic(it!!)
               findNavController(R.id.nav_host_fragment).navigate(R.id.action_homeFragment_to_playerFragment)
           }
        }
    }
}