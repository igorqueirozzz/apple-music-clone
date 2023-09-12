package dev.queiroz.applemusic.model

import com.google.gson.annotations.SerializedName

data class Song(
    val artistName: String,
    val collectionName: String,
    val trackName: String,
   @SerializedName(value = "artworkUrl100") val albumImageUrl: String,
   @SerializedName(value = "previewUrl") val songUrl: String
)
