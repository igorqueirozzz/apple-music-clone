package dev.queiroz.applemusic.ui.player

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
//        val currentSong: Song? = appleMusicViewModel.currentSong.value
//        with(binding){
//            currentSong.let { song ->
//                playerArtistNameTextView.text = song?.artistName
//                playerSongNameTextView.text= song?.trackName
//                processImage(song!!.songUrl)
//            }
//        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private fun processImage(imageUrl: String){
        Glide.with(this).asBitmap().load(imageUrl).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                imageBitmap = resource
                applyImage()
                applyBackgroundEffect()
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit

        })
    }

    private fun applyImage(){
        imageBitmap.let {
            binding.playerImageView.setImageBitmap(it!!)
        }
    }

   private fun applyBackgroundEffect(){
       imageBitmap.let {
           Palette.from(it!!).generate { palette ->
               val swatchList = palette?.swatches
               if (swatchList != null && swatchList.isNotEmpty()){
                   swatchList.sortByDescending { it.population }

                   val colors = swatchList.take(3).map { it.rgb }.toIntArray()

                   val gradientDrawable = GradientDrawable(
                       GradientDrawable.Orientation.TOP_BOTTOM,
                       colors
                   )

                   binding.root.background = gradientDrawable
               }
           }
       }
   }
}