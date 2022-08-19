package com.realityexpander.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.realityexpander.spotifyclone.R
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class AudioTrackAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseAudioTrackAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: AudioTrackViewHolder, position: Int) {
        val audioTrack = audioTracks[position]

        holder.itemView.apply {
            tvPrimary.text = audioTrack.title
            tvSecondary.text = audioTrack.subtitle
            glide.load(audioTrack.imageUrl).into(ivItemImage)

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(audioTrack)
                }
            }
        }
    }

}



















