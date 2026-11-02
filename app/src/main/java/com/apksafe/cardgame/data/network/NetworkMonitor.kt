package com.apksafe.cardgame.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class ConnectionStatus {
    WIFI,
    MOBILE_DATA,
    OFFLINE
}

object NetworkMonitor {

    fun getCurrentStatus(context: Context): ConnectionStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ConnectionStatus.OFFLINE

        val activeNetwork = cm.activeNetwork ?: return ConnectionStatus.OFFLINE
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return ConnectionStatus.OFFLINE

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionStatus.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionStatus.MOBILE_DATA
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionStatus.WIFI
            else -> ConnectionStatus.OFFLINE
        }
    }

    fun observeConnection(context: Context): Flow<ConnectionStatus> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            trySend(ConnectionStatus.OFFLINE)
            close()
            return@callbackFlow
        }

        // Send initial
        trySend(getCurrentStatus(context))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentStatus(context))
            }

            override fun onLost(network: Network) {
                trySend(getCurrentStatus(context))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getCurrentStatus(context))
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            trySend(getCurrentStatus(context))
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }
}
