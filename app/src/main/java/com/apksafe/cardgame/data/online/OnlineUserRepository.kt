package com.apksafe.cardgame.data.online

import android.content.Context
import com.apksafe.cardgame.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OnlineUserRepository(private val firestore: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun saveUserProfile(
        displayName: String,
        photoUrl: String?,
        deviceId: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid)
        val snapshot = docRef.get().await()

        if (!snapshot.exists()) {
            val payload = hashMapOf<String, Any?>(
                "userId" to uid,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "wins" to 0L,
                "losses" to 0L,
                "level" to 1L,
                "deviceId" to deviceId,
                "isOnline" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.set(payload).await()
        } else {
            val updates = hashMapOf<String, Any?>(
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "deviceId" to deviceId,
                "isOnline" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null }

            docRef.update(updates).await()
        }
    }

    suspend fun getUserProfile(userId: String): Result<OnlineUser?> = runCatching {
        val doc = firestore.collection("users").document(userId).get().await()
        doc.toObject(OnlineUser::class.java)
    }

    fun observeOnlineUsers(): Flow<List<OnlineUser>> {
        return firestore.collection("users")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(OnlineUser::class.java)
            }
    }

    suspend fun recordGameOutcome(isWin: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid)
        val fieldToIncrement = if (isWin) "wins" else "losses"
        docRef.update(
            mapOf(
                fieldToIncrement to FieldValue.increment(1),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
