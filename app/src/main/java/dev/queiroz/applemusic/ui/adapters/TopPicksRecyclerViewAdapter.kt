package dev.queiroz.applemusic.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.queiroz.applemusic.databinding.TopPickViewBinding
import dev.queiroz.applemusic.dummy.DummyMusic
import dev.queiroz.applemusic.model.Song

class TopPicksRecyclerViewAdapter : RecyclerView.Adapter<TopPicksRecyclerViewAdapter.ViewHolder>() {
    private val dummyMusic = DummyMusic().topPicks.shuffled()
    var onItemClickListener: ((Song) -> Unit?)? = null
    inner class ViewHolder(private val itemBinding: TopPickViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.root.setOnClickListener {
                onItemClickListener?.invoke(dummyMusic[bindingAdapterPosition])
            }
        }

        fun bindItem(song: Song) {
            itemBinding.tvPickName.text = song.artistName
            itemBinding.pickTextBottomCard.text = "${song.trackName} ${song.collectionName}"
            Glide.with(itemBinding.root).load(song.albumImageUrl).into(itemBinding.pickImageAlbum)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TopPickViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dummyMusic.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItem(dummyMusic[position])
    }
}