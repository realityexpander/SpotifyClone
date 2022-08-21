package com.realityexpander.spotifyclone.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.realityexpander.spotifyclone.R
import com.realityexpander.spotifyclone.adapters.AudioTrackAdapter
import com.realityexpander.spotifyclone.common.Status
import com.realityexpander.spotifyclone.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    // we don't use `val viewModel:MainViewModel by viewModels()` here because we want to
    // bind the ViewModel to the ACTIVITY lifecycle, not the fragment.

    @Inject
    lateinit var audioTrackAdapter: AudioTrackAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Not using the "by" delegate allows us to bind the viewModel to the lifecycle of the ACTIVITY (and not the fragment)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // explicitly bind the viewModel to the ACTIVITY lifecycle

        setupRecyclerView()
        subscribeToObservers()

        // Click on a track to play it
        audioTrackAdapter.setItemClickListener { audioTrack ->
            mainViewModel.playOrToggleAudioTrack(audioTrack)
        }
    }

    private fun setupRecyclerView() = rvAllAudioTracks.apply {
        adapter = audioTrackAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun subscribeToObservers() {
        mainViewModel.audioTracks.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.SUCCESS -> {
                    allSongsProgressBar.isVisible = false
                    result.payload?.let { audioTracks ->
                        audioTrackAdapter.audioTracks = audioTracks
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> allSongsProgressBar.isVisible = true
            }
        }
    }
}
















