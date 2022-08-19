package com.realityexpander.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.spotifyclone.data.entities.AudioTrack
import com.realityexpander.spotifyclone.other.Constants.AUDIO_TRACK_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val audioTrackCollection = firestore.collection(AUDIO_TRACK_COLLECTION)

    suspend fun getAllAudioTracks(): Pair<List<AudioTrack>, Boolean> {  // Pair<List<AudioTrack>, isSuccessful>
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