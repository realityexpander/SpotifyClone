package com.realityexpander.spotifyclone.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.realityexpander.spotifyclone.R
import com.realityexpander.spotifyclone.data.entities.AudioTrack
import com.realityexpander.spotifyclone.exoplayer.isPlaying
import com.realityexpander.spotifyclone.exoplayer.toAudioTrack
import com.realityexpander.spotifyclone.common.Status.SUCCESS
import com.realityexpander.spotifyclone.ui.viewmodels.MainViewModel
import com.realityexpander.spotifyclone.ui.viewmodels.AudioTrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_audio_track.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AudioTrackFragment : Fragment(R.layout.fragment_audio_track) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel
    private val audioTrackViewModel: AudioTrackViewModel by viewModels()

    private var curPlayingAudioTrack: AudioTrack? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()

        ivPlayPauseDetail.setOnClickListener {
            curPlayingAudioTrack?.let {
                mainViewModel.playOrToggleAudioTrack(it, true)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousAudioTrack()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextAudioTrack()
        }
    }

    private fun updateTitleAndSongImage(audioTrack: AudioTrack) {
        val title = "${audioTrack.title} - ${audioTrack.subtitle}"
        tvSongName.text = title
        glide.load(audioTrack.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.audioTracks.observe(viewLifecycleOwner) {
            it?.let { result ->
                when(result.status) {
                    SUCCESS -> {
                        result.payload?.let { audioTracks ->
                            if(curPlayingAudioTrack == null && audioTracks.isNotEmpty()) {
                                curPlayingAudioTrack = audioTracks[0]
                                updateTitleAndSongImage(audioTracks[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
        mainViewModel.curPlayingAudioTrack.observe(viewLifecycleOwner) {
            if(it == null) return@observe
            curPlayingAudioTrack = it.toAudioTrack()
            updateTitleAndSongImage(curPlayingAudioTrack!!)
        }
        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true)
                    R.drawable.ic_pause
                else
                    R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }
        audioTrackViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }
        audioTrackViewModel.curAudioTrackDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(ms)
    }
}





















