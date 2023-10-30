package dev.queiroz.applemusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.queiroz.applemusic.data.repository.ItunesRepository
import dev.queiroz.applemusic.dummy.DummyMusic
import dev.queiroz.applemusic.exoplayer.MusicServiceListener
import dev.queiroz.applemusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

abstract class MusicState {
    open val song: Song? = null
    open val songPosition: Pair<Int, Int>? = null

    data class Selected(
        override val song: Song? = null,
        override val songPosition: Pair<Int, Int>? = null
    ) : MusicState()

    data class Loading(
        override val song: Song? = null,
        override val songPosition: Pair<Int, Int>? = null
    ) : MusicState()

    data class Loaded(
        override val song: Song? = null,
        override val songPosition: Pair<Int, Int>? = null
    ) : MusicState()

    data class Playing(override val song: Song? = null, override val songPosition: Pair<Int, Int>) :
        MusicState()

    data class Stopped(
        override val song: Song? = null,
        override val songPosition: Pair<Int, Int>? = null
    ) : MusicState()

    data class Paused(override val song: Song? = null, override val songPosition: Pair<Int, Int>) :
        MusicState()

    data class OnPause(override val song: Song? = null, override val songPosition: Pair<Int, Int>? = null) :
        MusicState()

    data class OnUnpause(override val song: Song? = null, override val songPosition: Pair<Int, Int>? = null) :
        MusicState()

    data class ErrorOnLoad(
        override val song: Song? = null, val errorMessage: String,
        override val songPosition: Pair<Int, Int>? = null
    ) : MusicState()
}


@HiltViewModel
class AppleMusicViewModel @Inject constructor(private val itunesRepository: ItunesRepository) :
    ViewModel(), MusicServiceListener {
    private val _searchResultSongs = MutableLiveData<List<Song>>()
    val searchResultSongs: LiveData<List<Song>> = _searchResultSongs

    private val _songPosition = MutableLiveData<Pair<Int, Int>>()
    val songPosition: LiveData<Pair<Int, Int>> = _songPosition

    private val _currentSong =
        MutableStateFlow<MusicState>(value = MusicState.Selected(DummyMusic().topPicks.random()))
    val currentSong: StateFlow<MusicState> = _currentSong

    private val _loadingSearch = MutableLiveData<Boolean>()
    val loadingSearch: LiveData<Boolean> = _loadingSearch

    private val _isSongPlaying = MutableLiveData<Boolean>()
    val isSongPlaying: LiveData<Boolean> = _isSongPlaying

    fun findSongsByTerm(term: String) {
        GlobalScope.launch {
            _loadingSearch.postValue(true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = itunesRepository.findSongsByTerm(term)
            withContext(Dispatchers.Main) {
                if (result.isSuccessful) {
                    _searchResultSongs.value = result.body()?.songs?.map {
                        Song(
                            artistName = it.artistName,
                            trackName = it.trackName,
                            songUrl = it.songUrl,
                            collectionName = it.collectionName,
                            albumImageUrl = it.albumImageUrl?.replace("100x100", "500x500")
                        )
                    }
                } else {
                    println(result.message())
                }
                _loadingSearch.postValue(false)
            }

        }
    }

    fun onPauseMusic() {
        viewModelScope.launch {
            _currentSong.emit(
                value = MusicState.OnPause(
                    _currentSong.value.song,
                    _currentSong.value.songPosition!!,
                )
            )
        }
    }

    fun unpauseMusic() {
        viewModelScope.launch {
            _currentSong.emit(
                value = MusicState.OnUnpause(
                    _currentSong.value.song,
                    _currentSong.value.songPosition!!,
                )
            )
        }
    }

    fun playMusic(song: Song) {
        viewModelScope.launch {
            _currentSong.emit(value = MusicState.Loading(song = song))
        }
    }

    override fun onStateChanged(musicState: MusicState) {
        viewModelScope.launch {
            val oldSong = _currentSong.value.song
            val oldPosition = _currentSong.value.songPosition

            when (musicState) {
                is MusicState.Selected -> _currentSong.emit(
                    value = MusicState.Selected(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition ?: oldPosition
                    )
                )

                is MusicState.Loading -> _currentSong.emit(
                    value = MusicState.Loading(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition ?: oldPosition
                    )
                )

                is MusicState.Loaded -> _currentSong.emit(
                    value = MusicState.Loaded(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition ?: oldPosition
                    )
                )

                is MusicState.Playing -> _currentSong.emit(
                    value = MusicState.Playing(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition
                    )
                )

                is MusicState.Paused -> _currentSong.emit(
                    value = MusicState.Paused(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition
                    )
                )

                is MusicState.Stopped -> _currentSong.emit(
                    value = MusicState.Stopped(
                        song = musicState.song ?: oldSong,
                        songPosition = musicState.songPosition ?: oldPosition
                    )
                )

                is MusicState.ErrorOnLoad -> _currentSong.emit(
                    value = MusicState.ErrorOnLoad(
                        song = musicState.song ?: oldSong,
                        errorMessage = musicState.errorMessage
                    )
                )
            }
        }
    }

}