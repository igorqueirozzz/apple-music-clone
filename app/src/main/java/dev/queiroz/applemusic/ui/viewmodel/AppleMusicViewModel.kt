package dev.queiroz.applemusic.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.queiroz.applemusic.data.repository.ItunesRepository
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AppleMusicViewModel @Inject constructor(private val itunesRepository: ItunesRepository, private val mediaController: ListenableFuture<MediaController>) :
    ViewModel() {
    private val _searchResultSongs = MutableLiveData<List<Song>>()
    val searchResultSongs: LiveData<List<Song>> = _searchResultSongs
    fun findSongsByTerm(term: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = itunesRepository.findSongsByTerm(term)
            if (result.isSuccessful) {
                withContext(Dispatchers.Main){
                    _searchResultSongs.value = result.body()?.songs
                  _searchResultSongs.value?.first().let {
                      if(it != null){
                          mediaController.get().addMediaItem(MediaItem.Builder().setUri(it.songUrl).build())
                          mediaController.get().play()
                      }
                  }
                }
            } else {
                println(result.message())
            }
        }
    }

}