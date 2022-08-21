package com.realityexpander.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.spotifyclone.data.entities.AudioTrack
import com.realityexpander.spotifyclone.common.Constants.AUDIO_TRACK_COLLECTION
import kotlinx.coroutines.tasks.await

class AudioDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val audioTrackCollection = firestore.collection(AUDIO_TRACK_COLLECTION)

    // returns Pair<List<AudioTrack>, isSuccessful>
    suspend fun getAllAudioTracks(): Pair<List<AudioTrack>, Boolean> {
        return try {
            Pair(audioTrackCollection
                    .get()
                    .await()
                    .toObjects(AudioTrack::class.java),
                true)
        } catch(e: Exception) {
            e.printStackTrace()
           Pair(emptyList(), false)  // failed to load audio tracks
        }
    }
}