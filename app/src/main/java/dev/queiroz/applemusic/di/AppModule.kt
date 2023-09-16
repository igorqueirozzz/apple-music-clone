package dev.queiroz.applemusic.di

import android.content.ComponentName
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.common.util.concurrent.ListenableFuture
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import dagger.hilt.components.SingletonComponent
import dev.queiroz.applemusic.R
import dev.queiroz.applemusic.data.Constants
import dev.queiroz.applemusic.data.api.ItunesApiService
import dev.queiroz.applemusic.exoplayer.MusicService
import dev.queiroz.applemusic.ui.viewmodel.AppleMusicViewModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.ic_downloading)
            .error(R.drawable.ic_image_not_supported).diskCacheStrategy(DiskCacheStrategy.DATA)
    )

    @Provides
    fun provideBaseUrl(): String = Constants.BASE_URL

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
            .client(httpClient).build()

    @Provides
    @Singleton
    fun provideItunesApiService(retrofit: Retrofit): ItunesApiService =
        retrofit.create(ItunesApiService::class.java)

    @Provides
    @Singleton
    fun provideMediaSessionToken(@ApplicationContext context: Context): SessionToken =
        SessionToken(context, ComponentName(context, MusicService::class.java))

    @Provides
    @Singleton
    fun provideMediaController(@ApplicationContext context: Context, sessionToken: SessionToken): ListenableFuture<MediaController> = MediaController.Builder(context, sessionToken).buildAsync()
}