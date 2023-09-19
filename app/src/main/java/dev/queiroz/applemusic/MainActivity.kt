package dev.queiroz.applemusic

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
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
        setContentView(binding.root)
        setupBottomNavigation()
        val serviceConnectionIntent = Intent(this, MusicService::class.java)
        bindService(serviceConnectionIntent, musicServiceConnection, 0)
        setupFloatingMiniPlayer()
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

    private fun setupFloatingMiniPlayer() {
      //  val seekBar = findViewById<Slider>(R.id.floatingMiniPlayerProgress)
        val imageView = findViewById<ImageView>(R.id.floatingMiniPlayerImageView)
        val songNameTextView = findViewById<TextView>(R.id.floatingMiniPlayerSongName)
//        seekBar.run {
//            setOnTouchListener { p0, p1 -> true }
//        }
//        appleMusicViewModel.songPosition.observe(this) {
//                seekBar.value = it.first.toFloat()
//                seekBar.valueTo = it.second.toFloat()
//            }

        appleMusicViewModel.currentSong.observe(this@MainActivity) {
           musicService?.getPlayer()?.setMediaItem(MediaItem.Builder().setUri(it.songUrl).build())
            Glide
                .with(this@MainActivity)
                .load(it.albumImageUrl)
                .into(imageView)
            songNameTextView.text = it.trackName
        }
    }
}