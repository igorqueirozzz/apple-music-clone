package dev.queiroz.applemusic.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.queiroz.applemusic.data.repository.ItunesRepository
import dev.queiroz.applemusic.dummy.DummyMusic
import dev.queiroz.applemusic.exoplayer.MusicServiceListener
import dev.queiroz.applemusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppleMusicViewModel @Inject constructor(private val itunesRepository: ItunesRepository) :
    ViewModel(), MusicServiceListener {
    private val _searchResultSongs = MutableLiveData<List<Song>>()
    val searchResultSongs: LiveData<List<Song>> = _searchResultSongs

    private val _songPosition = MutableLiveData<Pair<Int, Int>>()
    val songPosition: LiveData<Pair<Int, Int>> = _songPosition

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    private val _loadingSearch = MutableLiveData<Boolean>()
    val loadingSearch: LiveData<Boolean> = _loadingSearch

    private val _isSongPlaying = MutableLiveData<Boolean>()
    val isSongPlaying: LiveData<Boolean> = _isSongPlaying

    init {
        _currentSong.value = DummyMusic().topPicks.random()
    }

    fun findSongsByTerm(term: String) {
        GlobalScope.launch {
            _loadingSearch.postValue(true)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = itunesRepository.findSongsByTerm(term)
            withContext(Dispatchers.Main){
                if (result.isSuccessful) {
                    _searchResultSongs.value = result.body()?.songs
                } else {
                    println(result.message())
                }
                _loadingSearch.postValue(false)
            }

        }
    }

    override fun onMusicProgressUpdate(currentPosition: Int, totalPosition: Int) {
        _songPosition.value = Pair(first = currentPosition, second = totalPosition)
    }

    override fun onPauseMusic() {
        _isSongPlaying.value = false
        _songPosition.value = Pair(first = 0, second = 1)
    }

    override fun onPlayMusic() { _isSongPlaying.value = true }

    fun playMusic(song: Song){
        _currentSong.value = song
    }

}