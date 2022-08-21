package com.realityexpander.spotifyclone.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.realityexpander.spotifyclone.R
import com.realityexpander.spotifyclone.adapters.SwipeAudioTrackAdapter
import com.realityexpander.spotifyclone.data.entities.AudioTrack
import com.realityexpander.spotifyclone.exoplayer.isPlaying
import com.realityexpander.spotifyclone.exoplayer.toAudioTrack
import com.realityexpander.spotifyclone.common.Status.*
import com.realityexpander.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

// Firebase Dashboard:
// Database: https://console.cloud.google.com/firestore/data/songs/test_song6?authuser=0&project=spotifyclone-d1f1c
// storage: https://console.firebase.google.com/u/0/project/spotifyclone-d1f1c/storage/spotifyclone-d1f1c.appspot.com/files

// Music source: https://freemusicarchive.org/genre/New_Age/

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeAudioTrackAdapter: SwipeAudioTrackAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingAudioTrack: AudioTrack? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subscribeToObservers()

        vpAudioTrack.adapter = swipeAudioTrackAdapter

        vpAudioTrack.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (playbackState?.isPlaying == true) {
                        mainViewModel.playOrToggleAudioTrack(swipeAudioTrackAdapter.audioTracks[position])
                    } else {
                        curPlayingAudioTrack = swipeAudioTrackAdapter.audioTracks[position]
                    }
                }
            })

        ivPlayPause.setOnClickListener {
            curPlayingAudioTrack?.let {
                mainViewModel.playOrToggleAudioTrack(it, true)
            }
        }

        swipeAudioTrackAdapter.setItemClickListener {
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }
    }

    private fun hideBottomBar() {
        ivCurSongImage.isVisible = false
        vpAudioTrack.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        ivCurSongImage.isVisible = true
        vpAudioTrack.isVisible = true
        ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToCurrentSong(audioTrack: AudioTrack) {
        val newItemIndex = swipeAudioTrackAdapter.audioTracks.indexOf(audioTrack)

        if (newItemIndex != -1) { // -1 == not found
            vpAudioTrack.currentItem = newItemIndex
            curPlayingAudioTrack = audioTrack
        }
    }

    private fun subscribeToObservers() {

        // receive the list of audio tracks from the view model (from firebase)
        mainViewModel.audioTracks.observe(this) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.payload?.let { audioTracks ->
                            // load the adapter with the audio tracks
                            swipeAudioTrackAdapter.audioTracks = audioTracks

                            // Display the album art for the first song in the list
                            if (audioTracks.isNotEmpty()) {
                                glide.load((curPlayingAudioTrack ?: audioTracks[0]).imageUrl)
                                    .into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingAudioTrack ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }
            }
        }

        mainViewModel.curPlayingAudioTrack.observe(this) {
            if (it == null) return@observe

            curPlayingAudioTrack = it.toAudioTrack()  // convert to domain model
            glide.load(curPlayingAudioTrack?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingAudioTrack ?: return@observe)
        }

        // Everytime the playback state changes, this is called
        mainViewModel.playbackState.observe(this) {
            playbackState = it

            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true)
                    R.drawable.ic_pause
                else
                    R.drawable.ic_play
            )
        }

        // Show the the MediaBrowser connection status
        mainViewModel.isConnected.observe(this) {

            // Shows the message only once (should use channels instead)
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }

        // Show the network connection status (to the firebase server)
        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}























