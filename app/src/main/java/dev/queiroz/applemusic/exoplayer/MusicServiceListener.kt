package dev.queiroz.applemusic.exoplayer

interface MusicServiceListener {
    fun onMusicProgressUpdate(currentPosition: Int, totalPosition: Int)
    fun onPauseMusic()
    fun onPlayMusic()
}