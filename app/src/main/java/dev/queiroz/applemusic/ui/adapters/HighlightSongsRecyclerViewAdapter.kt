package dev.queiroz.applemusic.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.queiroz.applemusic.databinding.HighlightMusicCoverViewBinding
import dev.queiroz.applemusic.dummy.DummyMusic
import dev.queiroz.applemusic.model.Song

class HighlightSongsRecyclerViewAdapter :
    RecyclerView.Adapter<HighlightSongsRecyclerViewAdapter.ViewHolder>() {

    val dummyMusic = DummyMusic()

    class ViewHolder(private val itemBinding: HighlightMusicCoverViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(song: Song) {
            itemBinding.textViewHighlightTitle.text = song.artistName
            itemBinding.textViewHighlightTitle.text = song.trackName
            Glide
                .with(itemBinding.root)
                .load(song.albumImageUrl)
                .into(itemBinding.imageViewHighlight)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = HighlightMusicCoverViewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dummyMusic.songs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(dummyMusic.songs[position])
    }
}