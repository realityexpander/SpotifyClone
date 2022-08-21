package com.realityexpander.spotifyclone.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.realityexpander.spotifyclone.data.remote.AudioDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = AudioDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): SimpleExoPlayer =
        SimpleExoPlayer
            .Builder(context)
            .build().apply {
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)  // Pauses the player when headphones are plugged/unplugged
            }

    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    ): DefaultDataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "SpotifyClone App"))
}












