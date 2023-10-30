package dev.queiroz.applemusic.exoplayer

import dev.queiroz.applemusic.ui.viewmodel.MusicState

interface MusicServiceListener {
    fun onStateChanged(musicState: MusicState)
}