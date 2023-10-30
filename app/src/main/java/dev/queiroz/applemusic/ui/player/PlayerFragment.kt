package dev.queiroz.applemusic.ui.player

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.queiroz.applemusic.MainActivity
import dev.queiroz.applemusic.R
import dev.queiroz.applemusic.databinding.FragmentPlayerBinding
import dev.queiroz.applemusic.model.Song
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import dev.queiroz.applemusic.ui.viewmodel.MusicState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private val binding by lazy {
        FragmentPlayerBinding.inflate(layoutInflater)
    }

    var imageBitmap: Bitmap? = null

    private val appleMusicViewModel: AppleMusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentSong: Song? = appleMusicViewModel.currentSong.value.song
        with(binding) {
            currentSong.let { song ->
                playerArtistNameTextView.text = song?.artistName
                playerSongNameTextView.text = song?.trackName
                song!!.albumImageUrl?.let { processImage(it) }
            }
        }

        lifecycleScope.launch {
            setupObservers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainActivity).hideBottomComponents()
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).showBottomComponents()
    }

    private fun processImage(imageUrl: String) {
        Glide.with(this).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                imageBitmap = resource
                applyImage()
                applyColorScheme()
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit

        })
    }

    private fun applyImage() {
        imageBitmap.let {
            binding.playerImageView.setImageBitmap(it!!)
        }
    }

    private fun applyColorScheme() {
        imageBitmap?.let { bitmap ->
            Palette.from(bitmap).generate { palette ->
                val swatchList = palette?.swatches


                val swatchCopy = ArrayList(swatchList)

                swatchCopy.sortByDescending { it.population }


                val contrastColor = palette?.lightVibrantSwatch?.rgb ?: Color.WHITE
                val darkContrastColor = palette?.darkVibrantSwatch?.rgb ?: Color.DKGRAY

                val contrastColorColorStateList = ColorStateList.valueOf(contrastColor)

                with(binding) {
                    root.background = (palette?.mutedSwatch?.rgb ?: palette?.darkVibrantSwatch?.rgb
                    ?: Color.DKGRAY).toDrawable()
                    playerSongNameTextView.setTextColor(
                        palette?.lightVibrantSwatch?.rgb ?: Color.WHITE
                    )
                    playerArtistNameTextView.setTextColor(contrastColor)
                    playerProgress.trackTintList = ColorStateList.valueOf(darkContrastColor)
                    playerProgress.trackActiveTintList = contrastColorColorStateList
                    playerProgress.thumbTintList = contrastColorColorStateList
                    playerBackButton.imageTintList = contrastColorColorStateList
                    playerPlayButton.imageTintList = contrastColorColorStateList
                    playerNextButton.imageTintList = contrastColorColorStateList
                }
            }
        }
    }


    private suspend fun setupObservers() {
        appleMusicViewModel.currentSong.collectLatest { musicState ->
            val playerProgress = binding.playerProgress
            val playerPlayButton = binding.playerPlayButton
            val playerLoadingIndicator = binding.playerLoadingIndicator
            when (musicState) {
                is MusicState.Playing -> {
                    playerLoadingIndicator.hide()
                    playerProgress.visibility = View.VISIBLE
                    playerProgress.valueTo = musicState.songPosition.second.toFloat()
                    playerProgress.value = musicState.songPosition.first.toFloat()
                    playerPlayButton.setImageResource(R.drawable.ic_pause)
                    playerPlayButton.setOnClickListener { appleMusicViewModel.onPauseMusic() }
                }

                is MusicState.Loading -> {
                    playerLoadingIndicator.hide()
                    playerProgress.visibility = View.GONE
                    playerPlayButton.setOnClickListener { null }
                }

                is MusicState.Paused -> {
                    playerProgress.value = 0f
                    playerPlayButton.setImageResource(R.drawable.ic_play)
                    playerPlayButton.setOnClickListener { appleMusicViewModel.unpauseMusic() }
                }
            }
        }
    }
}