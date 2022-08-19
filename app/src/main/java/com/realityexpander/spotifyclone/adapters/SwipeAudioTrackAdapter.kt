package com.realityexpander.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.realityexpander.spotifyclone.R
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeAudioTrackAdapter : BaseAudioTrackAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: AudioTrackViewHolder, position: Int) {
        val audioTrack = audioTracks[position]

        holder.itemView.apply {
            val text = "${audioTrack.title} - ${audioTrack.subtitle}"
            tvPrimary.text = text

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(audioTrack)
                }
            }
        }
    }

}



















