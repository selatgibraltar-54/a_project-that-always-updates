package com.apksafe.cardgame.data.online

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class OnlineUser(
    @DocumentId val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val wins: Long = 0L,
    val losses: Long = 0L,
    val level: Long = 1L,
    val deviceId: String = "",
    val isOnline: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
