package io.devexpert.android_firebase.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import io.devexpert.android_firebase.model.Note
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreManager(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()

    private val auth = AuthManager(context)
    var userId = auth.getCurrentUser()?.uid

    suspend fun addNote(note: Note) {
        note.userId = userId.toString()
        firestore.collection("notes").add(note).await()
    }

    suspend fun updateNote(note: Note) {
        val noteRef = note.id?.let { firestore.collection("notes").document(it) }
        noteRef?.set(note)?.await()
    }

    suspend fun deleteNote(noteId: String) {
        val noteRef = firestore.collection("notes").document(noteId)
        noteRef.delete().await()
    }

    fun getNotesFlow(): Flow<List<Note>> = callbackFlow {
        val subscription = firestore.collection("notes").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Note::class.java)?.copy(id = doc.id)
                    } ?: emptyList()
                trySend(notes)
            }

        awaitClose { subscription.remove() }
    }
}