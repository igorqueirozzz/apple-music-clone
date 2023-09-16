package dev.queiroz.applemusic.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.queiroz.applemusic.data.repository.ItunesRepository
import dev.queiroz.applemusic.exoplayer.MusicProgressListener
import dev.queiroz.applemusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppleMusicViewModel @Inject constructor(private val itunesRepository: ItunesRepository) :
    ViewModel(), MusicProgressListener {
    private val _searchResultSongs = MutableLiveData<List<Song>>()
    val searchResultSongs: LiveData<List<Song>> = _searchResultSongs

    private val _songPosition = MutableLiveData<Pair<Int, Int>>()
    val songPosition: LiveData<Pair<Int, Int>> = _songPosition

    private val _currentSong = MutableLiveData<Song>()
    val currentSong: LiveData<Song> = _currentSong

    fun findSongsByTerm(term: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = itunesRepository.findSongsByTerm(term)
            if (result.isSuccessful) {
                withContext(Dispatchers.Main){
                    _searchResultSongs.value = result.body()?.songs
                    _searchResultSongs.value?.first().let {
                        if(it != null){
                            _currentSong.value = it
                        }
                    }
                }
            } else {
                println(result.message())
            }
        }
    }

    override fun onMusicProgressUpdate(currentPosition: Int, totalPosition: Int) {
        _songPosition.value = Pair(first = currentPosition, second = totalPosition)
    }

}