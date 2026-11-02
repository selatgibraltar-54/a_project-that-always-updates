package com.apksafe.cardgame.data.network.p2p

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.apksafe.cardgame.data.db.UserEntity
import com.apksafe.cardgame.data.network.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException

class P2PNetworkManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "P2PNetworkManager"
        const val UDP_DISCOVERY_PORT = 8988
        const val TCP_GAME_PORT = 8989
        private const val MAGIC_PREFIX = "CARD_P2P_V1"

        @Volatile
        private var INSTANCE: P2PNetworkManager? = null

        fun getInstance(context: Context): P2PNetworkManager {
            return INSTANCE ?: synchronized(this) {
                val inst = P2PNetworkManager(context.applicationContext)
                INSTANCE = inst
                inst
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _discoveredPlayers = MutableStateFlow<List<NetworkPlayer>>(emptyList())
    val discoveredPlayers: StateFlow<List<NetworkPlayer>> = _discoveredPlayers.asStateFlow()

    private val _connectionState = MutableStateFlow<P2PConnectionState>(P2PConnectionState.Disconnected)
    val connectionState: StateFlow<P2PConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableStateFlow<String?>(null)
    val incomingMessages: StateFlow<String?> = _incomingMessages.asStateFlow()

    private var multicastLock: WifiManager.MulticastLock? = null
    private var udpSocket: DatagramSocket? = null
    private var isListeningUdp = false

    private var serverSocket: ServerSocket? = null
    private var connectedSocket: Socket? = null
    private var socketWriter: PrintWriter? = null
    private var socketReader: BufferedReader? = null

    private var activeUser: UserEntity? = null
    private var isHosting = false

    fun startDiscovery(currentUser: UserEntity) {
        activeUser = currentUser
        acquireMulticastLock()

        if (!isListeningUdp) {
            isListeningUdp = true
            startUdpReceiver()
            startBroadcastBeaconJob()
            startCleanupJob()
        }
        // Immediately broadcast presence
        broadcastBeacon(currentUser, isHost = isHosting)
    }

    fun stopDiscovery() {
        isListeningUdp = false
        try {
            udpSocket?.close()
        } catch (_: Exception) {}
        udpSocket = null
        releaseMulticastLock()
    }

    private fun acquireMulticastLock() {
        try {
            if (multicastLock == null) {
                val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                multicastLock = wifiManager?.createMulticastLock("CardBattleDiscoveryLock")?.apply {
                    setReferenceCounted(true)
                    acquire()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire multicast lock: ${e.message}")
        }
    }

    private fun releaseMulticastLock() {
        try {
            multicastLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (_: Exception) {}
        multicastLock = null
    }

    fun broadcastBeacon(user: UserEntity, isHost: Boolean) {
        scope.launch {
            try {
                val thisDeviceId = DeviceUtils.getThisDeviceId(context)
                val payload = "$MAGIC_PREFIX|BEACON|${user.username}|$thisDeviceId|${user.avatar}|${user.level}|${user.wins}|$TCP_GAME_PORT|$isHost"
                val bytes = payload.toByteArray(Charsets.UTF_8)
                val socket = DatagramSocket()
                socket.broadcast = true

                // Try subnet broadcast and standard broadcast
                val broadcastAddresses = listOf(
                    InetAddress.getByName("255.255.255.255"),
                    InetAddress.getByName("192.168.43.255") // Hotspot default broadcast
                )

                for (addr in broadcastAddresses) {
                    try {
                        val packet = DatagramPacket(bytes, bytes.size, addr, UDP_DISCOVERY_PORT)
                        socket.send(packet)
                    } catch (_: Exception) {}
                }
                socket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error broadcasting beacon: ${e.message}")
            }
        }
    }

    private fun startBroadcastBeaconJob() {
        scope.launch {
            while (isActive && isListeningUdp) {
                activeUser?.let { user ->
                    broadcastBeacon(user, isHosting)
                }
                delay(3000)
            }
        }
    }

    private fun startCleanupJob() {
        scope.launch {
            while (isActive && isListeningUdp) {
                val now = System.currentTimeMillis()
                _discoveredPlayers.value = _discoveredPlayers.value.filter { now - it.lastSeenMs < 10000 }
                delay(3000)
            }
        }
    }

    private fun startUdpReceiver() {
        scope.launch {
            try {
                val socket = DatagramSocket(UDP_DISCOVERY_PORT)
                udpSocket = socket
                socket.broadcast = true
                val buffer = ByteArray(2048)

                val thisDeviceId = DeviceUtils.getThisDeviceId(context)

                while (isActive && isListeningUdp) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)

                    val senderIp = packet.address.hostAddress ?: continue
                    val text = String(packet.data, 0, packet.length, Charsets.UTF_8).trim()

                    if (!text.startsWith(MAGIC_PREFIX)) continue
                    val parts = text.split("|")
                    if (parts.size >= 9 && parts[1] == "BEACON") {
                        val remoteUsername = parts[2]
                        val remoteDeviceId = parts[3]
                        val avatar = parts[4]
                        val level = parts[5].toIntOrNull() ?: 1
                        val wins = parts[6].toIntOrNull() ?: 0
                        val port = parts[7].toIntOrNull() ?: TCP_GAME_PORT
                        val remoteIsHost = parts[8].toBoolean()

                        // CRITICAL: Do NOT list ourselves or any profile from this same device!
                        if (remoteDeviceId == thisDeviceId) {
                            continue
                        }

                        val player = NetworkPlayer(
                            username = remoteUsername,
                            originDeviceId = remoteDeviceId,
                            avatar = avatar,
                            level = level,
                            wins = wins,
                            ipAddress = senderIp,
                            port = port,
                            isHost = remoteIsHost,
                            lastSeenMs = System.currentTimeMillis()
                        )

                        // Update or add
                        val currentList = _discoveredPlayers.value.toMutableList()
                        val existingIndex = currentList.indexOfFirst { it.originDeviceId == remoteDeviceId }
                        if (existingIndex >= 0) {
                            currentList[existingIndex] = player
                        } else {
                            currentList.add(player)
                        }
                        _discoveredPlayers.value = currentList
                    }
                }
            } catch (e: Exception) {
                if (isListeningUdp) {
                    Log.w(TAG, "UDP listener closed or error: ${e.message}")
                }
            }
        }
    }

    // ==========================================
    // TCP SERVER / HOST ROOM
    // ==========================================
    fun hostRoom(currentUser: UserEntity, onOpponentJoined: (NetworkPlayer) -> Unit) {
        activeUser = currentUser
        isHosting = true
        broadcastBeacon(currentUser, isHost = true)

        val localIp = DeviceUtils.getLocalIpAddress()
        _connectionState.value = P2PConnectionState.Hosting(localIp, TCP_GAME_PORT)

        scope.launch {
            try {
                serverSocket?.close()
                val server = ServerSocket(TCP_GAME_PORT)
                serverSocket = server
                server.soTimeout = 180000 // 3 min wait

                val client = server.accept()
                connectedSocket = client
                val reader = BufferedReader(InputStreamReader(client.getInputStream(), Charsets.UTF_8))
                val writer = PrintWriter(client.getOutputStream(), true)
                socketReader = reader
                socketWriter = writer

                // Receive Handshake
                val handshakeLine = reader.readLine()
                if (handshakeLine != null && handshakeLine.startsWith("HANDSHAKE|")) {
                    val p = handshakeLine.split("|")
                    val clientUsername = p.getOrNull(1) ?: "Player"
                    val clientDeviceId = p.getOrNull(2) ?: ""
                    val clientAvatar = p.getOrNull(3) ?: "phoenix"
                    val clientLevel = p.getOrNull(4)?.toIntOrNull() ?: 1
                    val clientWins = p.getOrNull(5)?.toIntOrNull() ?: 0

                    val thisDeviceId = DeviceUtils.getThisDeviceId(context)

                    // ENFORCE USER RULE: Dilarang memakai akun dari device yang sama!
                    if (clientDeviceId.isEmpty() || clientDeviceId == thisDeviceId) {
                        writer.println("REJECT|SAME_DEVICE_FORBIDDEN|Tidak boleh memakai akun dari perangkat yang sama!")
                        client.close()
                        _connectionState.value = P2PConnectionState.Error("❌ Koneksi Ditolak: Perangkat lawan sama dengan perangkat ini!")
                        return@launch
                    }

                    // Send Handshake OK with Host Profile
                    writer.println("ACCEPT|${currentUser.username}|$thisDeviceId|${currentUser.avatar}|${currentUser.level}|${currentUser.wins}")

                    val opponent = NetworkPlayer(
                        username = clientUsername,
                        originDeviceId = clientDeviceId,
                        avatar = clientAvatar,
                        level = clientLevel,
                        wins = clientWins,
                        ipAddress = client.inetAddress.hostAddress ?: "remote",
                        port = TCP_GAME_PORT,
                        isHost = false
                    )

                    _connectionState.value = P2PConnectionState.Connected(opponent, isHost = true)
                    withContext(Dispatchers.Main) {
                        onOpponentJoined(opponent)
                    }

                    // Start message read loop
                    startMessageReaderLoop(reader)
                } else {
                    client.close()
                    _connectionState.value = P2PConnectionState.Error("Handshake gagal.")
                }
            } catch (e: Exception) {
                if (_connectionState.value is P2PConnectionState.Hosting) {
                    _connectionState.value = P2PConnectionState.Error("Host room dibatalkan atau waktu tunggu habis: ${e.message}")
                }
            }
        }
    }

    // ==========================================
    // TCP CLIENT / JOIN ROOM
    // ==========================================
    fun joinRoom(
        currentUser: UserEntity,
        hostIp: String,
        hostPort: Int = TCP_GAME_PORT,
        onConnected: (NetworkPlayer) -> Unit,
        onError: (String) -> Unit
    ) {
        activeUser = currentUser
        _connectionState.value = P2PConnectionState.Connecting(hostIp)

        scope.launch {
            try {
                val socket = Socket(hostIp, hostPort)
                connectedSocket = socket
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                socketWriter = writer
                socketReader = reader

                val thisDeviceId = DeviceUtils.getThisDeviceId(context)

                // Send Handshake
                writer.println("HANDSHAKE|${currentUser.username}|$thisDeviceId|${currentUser.avatar}|${currentUser.level}|${currentUser.wins}")

                // Read Response
                val response = reader.readLine()
                if (response != null && response.startsWith("ACCEPT|")) {
                    val p = response.split("|")
                    val hostUsername = p.getOrNull(1) ?: "Host"
                    val hostDeviceId = p.getOrNull(2) ?: ""
                    val hostAvatar = p.getOrNull(3) ?: "knight"
                    val hostLevel = p.getOrNull(4)?.toIntOrNull() ?: 1
                    val hostWins = p.getOrNull(5)?.toIntOrNull() ?: 0

                    if (hostDeviceId == thisDeviceId) {
                        socket.close()
                        val msg = "❌ Dilarang: Host berada di perangkat yang sama!"
                        _connectionState.value = P2PConnectionState.Error(msg)
                        withContext(Dispatchers.Main) { onError(msg) }
                        return@launch
                    }

                    val hostPlayer = NetworkPlayer(
                        username = hostUsername,
                        originDeviceId = hostDeviceId,
                        avatar = hostAvatar,
                        level = hostLevel,
                        wins = hostWins,
                        ipAddress = hostIp,
                        port = hostPort,
                        isHost = true
                    )

                    _connectionState.value = P2PConnectionState.Connected(hostPlayer, isHost = false)
                    withContext(Dispatchers.Main) {
                        onConnected(hostPlayer)
                    }

                    // Start message loop
                    startMessageReaderLoop(reader)
                } else {
                    val err = if (response?.startsWith("REJECT|") == true) {
                        response.split("|").getOrNull(2) ?: "Koneksi ditolak oleh host."
                    } else {
                        "Gagal melakukan handshake dengan room."
                    }
                    socket.close()
                    _connectionState.value = P2PConnectionState.Error(err)
                    withContext(Dispatchers.Main) { onError(err) }
                }
            } catch (e: Exception) {
                val err = "Tidak dapat terhubung ke $hostIp:$hostPort (${e.localizedMessage ?: "Timeout"})"
                _connectionState.value = P2PConnectionState.Error(err)
                withContext(Dispatchers.Main) { onError(err) }
            }
        }
    }

    private fun startMessageReaderLoop(reader: BufferedReader) {
        scope.launch {
            try {
                while (isActive) {
                    val line = reader.readLine() ?: break
                    _incomingMessages.value = line
                }
            } catch (e: Exception) {
                Log.d(TAG, "Socket reader closed: ${e.message}")
            } finally {
                disconnect()
            }
        }
    }

    fun sendMessage(action: String) {
        scope.launch {
            try {
                socketWriter?.println(action)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send message: ${e.message}")
            }
        }
    }

    fun disconnect() {
        try {
            socketWriter?.close()
            socketReader?.close()
            connectedSocket?.close()
            serverSocket?.close()
        } catch (_: Exception) {}
        socketWriter = null
        socketReader = null
        connectedSocket = null
        serverSocket = null
        isHosting = false
        _connectionState.value = P2PConnectionState.Disconnected
    }
}
