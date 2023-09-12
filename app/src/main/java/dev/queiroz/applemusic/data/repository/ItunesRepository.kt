package dev.queiroz.applemusic.data.repository

import dev.queiroz.applemusic.data.api.ItunesApiService
import dev.queiroz.applemusic.model.SongResponse
import retrofit2.Response
import javax.inject.Inject

class ItunesRepository @Inject constructor(private val itunesApiService: ItunesApiService) {

    suspend fun findSongsByTerm(term: String): Response<SongResponse> =
        itunesApiService.findSongsByTerm(term)
}