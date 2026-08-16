package com.example.stagekeeper

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson

class MeshManager(
    private val context: Context,
    private val currentUserId: String,
    private val onPinReceived: (MeetupLocation) -> Unit
) {

    private val connectionsClient = Nearby.getConnectionsClient(context)

    // P2P_CLUSTER creates a web of devices (M-to-N)
    private val STRATEGY = Strategy.P2P_CLUSTER
    private val SERVICE_ID = "com.example.stagekeeper.MESH_NETWORK"

    private val TAG = "StageKeeperMesh"

    // Tracks devices we have successfully connected to
    private val connectedEndpoints = mutableSetOf<String>()

    private val gson = Gson()

    // --- 1. The Advertiser (Broadcasting: "I am here!") ---

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()

        connectionsClient.startAdvertising(
            currentUserId, // We broadcast our Firebase UID so friends know who we are
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Successfully started advertising!")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start advertising", e)
        }
    }

    // --- 2. The Discoverer (Listening: "Is anyone there?") ---

    fun startDiscovering() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Successfully started discovering!")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start discovering", e)
        }
    }

    // --- 3. The Handshake Protocols ---

    // What happens when we hear another StageKeeper phone advertising?
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found a device! Requesting connection to: $endpointId (User: ${info.endpointName})")

            // Instantly attempt to connect to them
            connectionsClient.requestConnection(currentUserId, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost sight of device: $endpointId")
        }
    }

    // What happens when a connection is actually requested and established?
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with: $endpointId. Accepting automatically.")
            // For a mesh network, we auto-accept connections to build the web silently
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "SUCCESS! Connected to device: $endpointId")
                    connectedEndpoints.add(endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "Connection rejected by: $endpointId")
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.d(TAG, "Connection error with: $endpointId")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: $endpointId")
            connectedEndpoints.remove(endpointId)
        }
    }

    // --- 4. The Data Receiver ---

    // What happens when an offline phone sends us a map pin over Bluetooth/Wi-Fi?
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { receivedBytes ->
                    try {
                        val jsonString = String(receivedBytes, Charsets.UTF_8)
                        Log.d(TAG, "RECEIVED OFFLINE DATA from $endpointId: $jsonString")

                        val receivedPin = gson.fromJson(jsonString, MeetupLocation::class.java)

                        // Shove it into the local Room database
                        onPinReceived(receivedPin)

                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse offline pin data", e)
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Can be used to track loading bars for big file transfers (not needed for small pins)
        }
    }

    // --- 5. The Data Broadcaster ---

    fun broadcastPin(pin: MeetupLocation) {
        if (connectedEndpoints.isEmpty()) {
            Log.d(TAG, "No offline devices connected to broadcast to.")
            return
        }

        try {
            val jsonString = gson.toJson(pin)
            val bytes = jsonString.toByteArray(Charsets.UTF_8)
            val payload = Payload.fromBytes(bytes)

            connectionsClient.sendPayload(connectedEndpoints.toList(), payload)
            Log.d(TAG, "BROADCASTED offline pin to ${connectedEndpoints.size} devices!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast pin", e)
        }
    }

    // Clean up when the app closes
    fun stopMesh() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
    }
}