package com.realityexpander.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.spotifyclone.data.entities.AudioTrack

abstract class BaseAudioTrackAdapter(
    private val layoutId: Int
) : RecyclerView.Adapter<BaseAudioTrackAdapter.AudioTrackViewHolder>() {

    class AudioTrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    protected val diffCallback = object : DiffUtil.ItemCallback<AudioTrack>() {
        override fun areItemsTheSame(oldItem: AudioTrack, newItem: AudioTrack): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: AudioTrack, newItem: AudioTrack): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    protected abstract val differ: AsyncListDiffer<AudioTrack>

    var audioTracks: List<AudioTrack>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioTrackViewHolder {
        return AudioTrackViewHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
                parent,
                false
            )
        )
    }

    protected var onItemClickListener: ((AudioTrack) -> Unit)? = null

    fun setItemClickListener(listener: (AudioTrack) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return audioTracks.size
    }
}
