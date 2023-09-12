package dev.queiroz.applemusic.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.queiroz.applemusic.databinding.HighlightMusicCoverViewBinding
import dev.queiroz.applemusic.databinding.SongRowLayoutBinding
import dev.queiroz.applemusic.dummy.DummyMusic
import dev.queiroz.applemusic.model.Song

class SongListRecyclerAdapter :
    RecyclerView.Adapter<SongListRecyclerAdapter.ViewHolder>() {
    private val listOfSongs: MutableList<Song> = mutableListOf()

    class ViewHolder(private val itemBinding: SongRowLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(song: Song) {
            itemBinding.tvArtistName.text = song.artistName
            itemBinding.tvSongName.text = song.trackName
            Glide
                .with(itemBinding.root)
                .load(song.albumImageUrl)
                .into(itemBinding.songImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = SongRowLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfSongs.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(listOfSongs[position])
    }

    fun updateListOfSongs(newSongs: List<Song>) {
        listOfSongs.clear()
        listOfSongs.addAll(newSongs)
        notifyDataSetChanged()
    }
}