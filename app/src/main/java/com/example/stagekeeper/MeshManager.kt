package com.example.stagekeeper

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

class MeshManager(
    private val context: Context,
    private val currentUserId: String,
    private val onDataReceived: (String) -> Unit
) {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val STRATEGY = Strategy.P2P_CLUSTER
    private val SERVICE_ID = "com.example.stagekeeper.MESH_NETWORK"
    private val TAG = "StageKeeperMesh"
    private val connectedEndpoints = mutableSetOf<String>()

    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(
            currentUserId,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "Successfully started advertising!")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to start advertising", e)
        }
    }

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

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Found a device! Requesting connection to: $endpointId (User: ${info.endpointName})")
            connectionsClient.requestConnection(currentUserId, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost sight of device: $endpointId")
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with: $endpointId. Accepting automatically.")
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

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { receivedBytes ->
                    try {
                        val dataString = String(receivedBytes, Charsets.UTF_8)
                        Log.d(TAG, "RECEIVED OFFLINE DATA from $endpointId: $dataString")
                        onDataReceived(dataString)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse offline data", e)
                    }
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {}
    }

    fun broadcastData(dataString: String) {
        if (connectedEndpoints.isEmpty()) return

        try {
            val bytes = dataString.toByteArray(Charsets.UTF_8)
            val payload = Payload.fromBytes(bytes)
            connectionsClient.sendPayload(connectedEndpoints.toList(), payload)
            Log.d(TAG, "BROADCASTED secure offline data to ${connectedEndpoints.size} devices!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast data", e)
        }
    }

    fun stopMesh() {
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
    }
}