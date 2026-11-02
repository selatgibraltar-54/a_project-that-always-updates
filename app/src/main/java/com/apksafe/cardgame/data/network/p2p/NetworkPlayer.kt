package com.apksafe.cardgame.data.network.p2p

import com.apksafe.cardgame.data.db.UserEntity

data class NetworkPlayer(
    val username: String,
    val originDeviceId: String,
    val avatar: String = "knight",
    val level: Int = 1,
    val wins: Int = 0,
    val ipAddress: String,
    val port: Int = 8989,
    val isHost: Boolean = false,
    val lastSeenMs: Long = System.currentTimeMillis()
) {
    fun toUserEntity(): UserEntity {
        return UserEntity(
            id = 9000L + (originDeviceId.hashCode().toLong().let { if (it < 0) -it else it } % 1000L),
            username = username,
            passwordHash = "",
            salt = "",
            avatar = avatar,
            coins = 200,
            level = level,
            xp = 50,
            wins = wins,
            losses = 0,
            matchesPlayed = wins,
            originDeviceId = originDeviceId
        )
    }
}

sealed class P2PConnectionState {
    object Disconnected : P2PConnectionState()
    data class Hosting(val localIp: String, val port: Int) : P2PConnectionState()
    data class Connecting(val targetIp: String) : P2PConnectionState()
    data class Connected(val opponent: NetworkPlayer, val isHost: Boolean) : P2PConnectionState()
    data class Error(val message: String) : P2PConnectionState()
}
