package com.realityexpander.spotifyclone.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.spotifyclone.data.entities.Song
import com.realityexpander.spotifyclone.other.Constants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {

    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    suspend fun getAllSongs(): Pair<List<Song>, Boolean> {  // Pair<List<Song>, isSuccessful>
        return try {
            Pair(songCollection
                    .get()
                    .await()
                    .toObjects(Song::class.java),
                true)
        } catch(e: Exception) {
            e.printStackTrace()
           Pair(emptyList(), false)  // failed to load songs
        }
    }
}