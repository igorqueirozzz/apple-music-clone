package dev.queiroz.applemusic.model
import com.google.gson.annotations.SerializedName

data class SongResponse(
    @SerializedName(value = "results") val songs: List<Song>
)