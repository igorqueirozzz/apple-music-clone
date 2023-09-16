package dev.queiroz.applemusic.exoplayer

interface MusicProgressListener {
    fun onMusicProgressUpdate(currentPosition: Int, totalPosition: Int)
}