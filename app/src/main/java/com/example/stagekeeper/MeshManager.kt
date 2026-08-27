package com.example.stagekeeper

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

class MeshManager(
    context: Context,
    private val currentUserId: String,
    private val onDataReceived: (String) -> Unit
) {

    companion object {
        private val STRATEGY = Strategy.P2P_CLUSTER
        private const val SERVICE_ID = "com.example.stagekeeper.MESH_NETWORK"
        private const val TAG = "StageKeeperMesh"
    }

    private val connectionsClient =
        Nearby.getConnectionsClient(context.applicationContext)

    private val connectedEndpoints = mutableSetOf<String>()
    private val pendingEndpoints = mutableSetOf<String>()

    private var isAdvertising = false
    private var isDiscovering = false

    // ============================================================
    // ADVERTISING
    // ============================================================

    fun startAdvertising() {
        if (isAdvertising) {
            Log.d(TAG, "Advertising already active. Skipping duplicate start.")
            return
        }

        // Set this before the asynchronous request so another call
        // cannot start advertising again while this one is pending.
        isAdvertising = true

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startAdvertising(
            currentUserId,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        )
            .addOnSuccessListener {
                Log.d(TAG, "Successfully started advertising!")
            }
            .addOnFailureListener { e ->
                isAdvertising = false
                Log.e(TAG, "Failed to start advertising", e)
            }
    }

    // ============================================================
    // DISCOVERY
    // ============================================================

    fun startDiscovering() {
        if (isDiscovering) {
            Log.d(TAG, "Discovery already active. Skipping duplicate start.")
            return
        }

        isDiscovering = true

        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        )
            .addOnSuccessListener {
                Log.d(TAG, "Successfully started discovering!")
            }
            .addOnFailureListener { e ->
                isDiscovering = false
                Log.e(TAG, "Failed to start discovering", e)
            }
    }

    // ============================================================
    // DEVICE DISCOVERY
    // ============================================================

    private val endpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {

            override fun onEndpointFound(
                endpointId: String,
                info: DiscoveredEndpointInfo
            ) {
                val remoteUserId = info.endpointName

                Log.d(
                    TAG,
                    "Found device: $endpointId (User: $remoteUserId)"
                )

                if (connectedEndpoints.contains(endpointId)) {
                    Log.d(
                        TAG,
                        "Already connected to $endpointId. Ignoring discovery."
                    )
                    return
                }

                if (pendingEndpoints.contains(endpointId)) {
                    Log.d(
                        TAG,
                        "Connection already pending for $endpointId."
                    )
                    return
                }

                /*
                 * IMPORTANT:
                 *
                 * Both StageKeeper devices advertise AND discover.
                 * Without a rule, both devices may call
                 * requestConnection() at the same time.
                 *
                 * We deterministically choose ONE device to initiate.
                 *
                 * Both devices see the same two user IDs, so:
                 *
                 * Device A:
                 *     A < B  -> initiates
                 *
                 * Device B:
                 *     B < A  -> waits
                 *
                 * Exactly one device initiates the connection.
                 */
                val shouldInitiateConnection =
                    currentUserId < remoteUserId

                if (!shouldInitiateConnection) {
                    Log.d(
                        TAG,
                        "Waiting for remote device $endpointId to initiate connection."
                    )
                    return
                }

                Log.d(
                    TAG,
                    "Initiating connection to $endpointId..."
                )

                pendingEndpoints.add(endpointId)

                connectionsClient.requestConnection(
                    currentUserId,
                    endpointId,
                    connectionLifecycleCallback
                )
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Connection request successfully sent to $endpointId."
                        )
                    }
                    .addOnFailureListener { e ->
                        pendingEndpoints.remove(endpointId)

                        Log.e(
                            TAG,
                            "Failed to request connection to $endpointId",
                            e
                        )
                    }
            }

            override fun onEndpointLost(endpointId: String) {
                Log.d(TAG, "Lost sight of device: $endpointId")

                if (!connectedEndpoints.contains(endpointId)) {
                    pendingEndpoints.remove(endpointId)
                }
            }
        }

    // ============================================================
    // CONNECTION LIFECYCLE
    // ============================================================

    private val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {

            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo
            ) {
                Log.d(
                    TAG,
                    "Connection initiated with $endpointId " +
                            "(User: ${connectionInfo.endpointName}). Accepting."
                )

                pendingEndpoints.add(endpointId)

                connectionsClient.acceptConnection(
                    endpointId,
                    payloadCallback
                )
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Accepted connection request from $endpointId."
                        )
                    }
                    .addOnFailureListener { e ->
                        pendingEndpoints.remove(endpointId)

                        Log.e(
                            TAG,
                            "Failed to accept connection from $endpointId",
                            e
                        )
                    }
            }

            override fun onConnectionResult(
                endpointId: String,
                result: ConnectionResolution
            ) {
                pendingEndpoints.remove(endpointId)

                when (result.status.statusCode) {

                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectedEndpoints.add(endpointId)

                        Log.d(
                            TAG,
                            "SUCCESS! Connected to device: $endpointId"
                        )
                    }

                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        connectedEndpoints.remove(endpointId)

                        Log.d(
                            TAG,
                            "Connection rejected by: $endpointId"
                        )
                    }

                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        connectedEndpoints.remove(endpointId)

                        Log.e(
                            TAG,
                            "Connection error with: $endpointId"
                        )
                    }

                    else -> {
                        connectedEndpoints.remove(endpointId)

                        Log.e(
                            TAG,
                            "Connection failed with $endpointId. " +
                                    "Status: ${result.status.statusCode}"
                        )
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                Log.d(
                    TAG,
                    "Disconnected from: $endpointId"
                )

                connectedEndpoints.remove(endpointId)
                pendingEndpoints.remove(endpointId)
            }
        }

    // ============================================================
    // PAYLOAD RECEIVING
    // ============================================================

    private val payloadCallback =
        object : PayloadCallback() {

            override fun onPayloadReceived(
                endpointId: String,
                payload: Payload
            ) {
                if (payload.type != Payload.Type.BYTES) {
                    return
                }

                val receivedBytes =
                    payload.asBytes() ?: return

                try {
                    val dataString =
                        String(receivedBytes, Charsets.UTF_8)

                    Log.d(
                        TAG,
                        "RECEIVED OFFLINE DATA from $endpointId: $dataString"
                    )

                    onDataReceived(dataString)

                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Failed to parse offline data",
                        e
                    )
                }
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                update: PayloadTransferUpdate
            ) {
                // Nothing needed for BYTES payloads right now.
            }
        }

    // ============================================================
    // SENDING
    // ============================================================

    fun broadcastData(dataString: String) {
        val endpoints =
            connectedEndpoints.toList()

        if (endpoints.isEmpty()) {
            Log.d(
                TAG,
                "No connected devices. Nothing to broadcast."
            )
            return
        }

        try {
            val payload = Payload.fromBytes(
                dataString.toByteArray(Charsets.UTF_8)
            )

            connectionsClient.sendPayload(
                endpoints,
                payload
            )
                .addOnSuccessListener {
                    Log.d(
                        TAG,
                        "BROADCASTED offline data to " +
                                "${endpoints.size} device(s)!"
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(
                        TAG,
                        "Failed to broadcast data",
                        e
                    )
                }

        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to create broadcast payload",
                e
            )
        }
    }

    // ============================================================
    // SHUTDOWN
    // ============================================================

    fun stopMesh() {
        Log.d(TAG, "Stopping mesh network")

        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()

        isAdvertising = false
        isDiscovering = false

        connectedEndpoints.clear()
        pendingEndpoints.clear()
    }
}