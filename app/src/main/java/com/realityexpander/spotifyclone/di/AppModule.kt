package com.realityexpander.spotifyclone.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.realityexpander.spotifyclone.R
import com.realityexpander.spotifyclone.adapters.SwipeAudioTrackAdapter
import com.realityexpander.spotifyclone.exoplayer.AudioServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ): AudioServiceConnection =
        AudioServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeAudioTrackAdapter(): SwipeAudioTrackAdapter =
        SwipeAudioTrackAdapter()

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ): RequestManager =
        Glide
            .with(context)
            .setDefaultRequestOptions(
                RequestOptions()
                    .placeholder(R.drawable.ic_image)
                    .error(R.drawable.ic_image)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
            )
}