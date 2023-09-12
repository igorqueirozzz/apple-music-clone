package dev.queiroz.applemusic.data.api

import dev.queiroz.applemusic.model.SongResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApiService {
    @GET("/search")
    suspend fun findSongsByTerm(
        @Query("term") term: String,
        @Query("media") media: String = "music"
    ): Response<SongResponse>
}