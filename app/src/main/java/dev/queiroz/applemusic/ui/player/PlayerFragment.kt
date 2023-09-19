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
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import dev.queiroz.applemusic.MainActivity
import dev.queiroz.applemusic.R
import dev.queiroz.applemusic.databinding.FragmentPlayerBinding
import dev.queiroz.applemusic.model.Song
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel

class PlayerFragment : Fragment() {

    private val binding by lazy {
        FragmentPlayerBinding.inflate(layoutInflater)
    }

    var imageBitmap: Bitmap? = null

    private val appleMusicViewModel: AppleMusicViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentSong: Song? = appleMusicViewModel.currentSong.value
        setupObservers()
        with(binding) {
            currentSong.let { song ->
                playerArtistNameTextView.text = song?.artistName
                playerSongNameTextView.text = song?.trackName
                processImage(song!!.albumImageUrl)
            }
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
                if (!swatchList.isNullOrEmpty()) {

                    val swatchCopy = ArrayList(swatchList)

                    swatchCopy.sortByDescending { it.population }

                    val colors = swatchList.take(3).map { it.rgb }.toIntArray()

                    val gradientDrawable = GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        colors
                    )
                    val contrastColor = chooseContrastColor(colors.toList())
                    val contrastColorColorStateList = ColorStateList.valueOf(contrastColor)

                    with(binding) {
                        root.background = gradientDrawable
                        playerSongNameTextView.setTextColor(contrastColor)
                        playerArtistNameTextView.setTextColor(contrastColor)
                        playerProgress.trackTintList = ColorStateList.valueOf(colors[2])
                        playerProgress.trackActiveTintList = contrastColorColorStateList
                        playerProgress.thumbTintList = contrastColorColorStateList
                        playerBackButton.imageTintList = contrastColorColorStateList
                        playerPlayButton.imageTintList = contrastColorColorStateList
                        playerNextButton.imageTintList = contrastColorColorStateList
                    }

                }
            }
        }
    }

    private fun chooseContrastColor(colors: List<Int>): Int {
        val defaultContrastColor = Color.WHITE

        for (color in colors) {
            val contrast = ColorUtils.calculateContrast(color, defaultContrastColor)
            if (contrast >= 10) {
                return color
            }
        }
        return defaultContrastColor
    }

    private fun setupObservers() {
        with(appleMusicViewModel) {
            songPosition.observe(this@PlayerFragment) {
                with(binding) {
                    playerProgress.valueTo = it.second.toFloat()
                    playerProgress.value = it.first.toFloat()
                }
            }

            isSongPlaying.observe(this@PlayerFragment) { isPlaying ->
                binding.playerPlayButton.setImageDrawable(
                    if (isPlaying) resources.getDrawable(R.drawable.ic_pause) else resources.getDrawable(
                        R.drawable.ic_play
                    )
                )
            }
        }
    }
}