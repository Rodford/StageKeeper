package com.example.stagekeeper

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagekeeper.data.PartyGroup
import com.example.stagekeeper.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val locationDao = database.locationDao()

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var meshManager: MeshManager? = null

    private val _availableParties = MutableStateFlow<List<PartyGroup>>(emptyList())
    val availableParties: StateFlow<List<PartyGroup>> = _availableParties

    // --- HIDDEN PINS BURN BOOK LOGIC ---

    private fun getHiddenPins(): Set<String> {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("hidden_pins", emptySet()) ?: emptySet()
    }

    private fun addHiddenPins(pinIds: List<String>) {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("hidden_pins", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.addAll(pinIds)
        prefs.edit().putStringSet("hidden_pins", current).apply()
    }

    // --- MESH NETWORK CONTROLS ---

    private fun initMeshEngine() {
        val uid = auth.currentUser?.uid ?: return
        meshManager = MeshManager(getApplication(), uid) { receivedPin ->
            viewModelScope.launch(Dispatchers.IO) {
                if (receivedPin.pinId == "COMMAND_CLEAR_ALL") {
                    // Admin commanded a network wipe. Hide current pins so they never return from local cache
                    val currentPins = allLocations.value.map { it.pinId }
                    addHiddenPins(currentPins)
                    locationDao.deleteAll()
                } else {
                    // Only save to Room if this user hasn't explicitly hidden this pin
                    if (!getHiddenPins().contains(receivedPin.pinId)) {
                        locationDao.insertLocation(receivedPin)
                    }
                }
            }
        }
    }

    fun turnOnOfflineMesh() {
        meshManager?.startAdvertising()
        meshManager?.startDiscovering()
    }

    fun turnOffOfflineMesh() {
        meshManager?.stopMesh()
    }

    // --- DYNAMIC PARTY LOGIC ---

    fun createNewParty(partyName: String, onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val inviteCode = UUID.randomUUID().toString().take(6).uppercase()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newPartyRef = firestore.collection("parties").document()
                val newParty = PartyGroup(partyId = newPartyRef.id, partyName = partyName, inviteCode = inviteCode, adminUserId = uid, memberIds = listOf(uid))
                newPartyRef.set(newParty).await()
                loadUserParties()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(inviteCode) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun joinParty(inviteCode: String, onResult: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val cleanCode = inviteCode.trim().uppercase()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("parties").whereEqualTo("inviteCode", cleanCode).get().await()
                if (snapshot.isEmpty) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "Invalid Invite Code.") }
                    return@launch
                }
                val partyDoc = snapshot.documents.first()
                firestore.collection("parties").document(partyDoc.id).update("memberIds", FieldValue.arrayUnion(uid)).await()
                loadUserParties()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(true, partyDoc.getString("partyName") ?: "") }
            } catch (e: Exception) { e.printStackTrace(); kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "Network error.") } }
        }
    }

    fun leaveParty(partyName: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val party = _availableParties.value.find { it.partyName == partyName } ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("parties").document(party.partyId).update("memberIds", FieldValue.arrayRemove(uid)).await()
                loadUserParties()
                turnOffOfflineMesh()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    private fun loadUserParties() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("parties").whereArrayContains("memberIds", uid).get().await()
                val parties = snapshot.documents.mapNotNull { it.toObject(PartyGroup::class.java) }
                _availableParties.value = parties
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- MAP PIN SYNC LOGIC ---

    val allLocations: StateFlow<List<MeetupLocation>> = locationDao.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var pinListener: ListenerRegistration? = null

    fun startListeningToPartyPins(partyName: String) {
        pinListener?.remove()
        val party = _availableParties.value.find { it.partyName == partyName } ?: return

        val pinsRef = firestore.collection("parties").document(party.partyId).collection("pins")

        // 1. AGGRESSIVE CLOUD SYNC: Bypass the lazy local cache and demand the real history from Google's servers
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Source.SERVER forces a network call. It will fail silently if totally offline.
                val serverSnapshot = pinsRef.get(com.google.firebase.firestore.Source.SERVER).await()
                val hidden = getHiddenPins()

                serverSnapshot.toObjects(MeetupLocation::class.java).forEach { pin ->
                    if (!hidden.contains(pin.pinId)) {
                        locationDao.insertLocation(pin)
                    }
                }
                android.util.Log.d("CloudSync", "Successfully pulled ${serverSnapshot.size()} pins from the cloud!")
            } catch (e: Exception) {
                android.util.Log.d("CloudSync", "Offline. Relying on local cache and Mesh Network.")
            }
        }

        // 2. Keep the real-time listener active for live updates while online
        pinListener = pinsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            viewModelScope.launch(Dispatchers.IO) {
                val hidden = getHiddenPins()
                val cloudPins = snapshot.toObjects(MeetupLocation::class.java)
                cloudPins.forEach { pin ->
                    // Only sync pins from cloud if the user hasn't explicitly hidden them
                    if (!hidden.contains(pin.pinId)) {
                        locationDao.insertLocation(pin)
                    }
                }
            }
        }
    }

    fun saveLocationToDatabase(lat: Double, lng: Double, note: String, activePartyName: String) {
        val party = _availableParties.value.find { it.partyName == activePartyName }
        val partyId = party?.partyId ?: ""
        val pinId = UUID.randomUUID().toString()

        val loc = MeetupLocation(pinId = pinId, partyId = partyId, latitude = lat, longitude = lng, note = note)

        viewModelScope.launch(Dispatchers.IO) {
            locationDao.insertLocation(loc)
            meshManager?.broadcastPin(loc)

            if (partyId.isNotBlank()) {
                // Firebase automatically queues this if offline, and sends it when online
                firestore.collection("parties").document(partyId).collection("pins").document(pinId).set(loc)
                    .addOnSuccessListener {
                        android.util.Log.d("CloudSync", "Pin successfully uploaded to cloud database!")
                    }
            }
        }
    }

    fun deleteAllLocations(activePartyName: String) {
        val uid = auth.currentUser?.uid ?: return
        val party = _availableParties.value.find { it.partyName == activePartyName } ?: return

        viewModelScope.launch(Dispatchers.IO) {
            // 1. Hide all current pins locally so they never come back from offline cache
            val currentPins = allLocations.value.map { it.pinId }
            addHiddenPins(currentPins)
            locationDao.deleteAll()

            // 2. ONLY broadcast the Kill Switch and wipe the Cloud if this user is the Admin!
            if (party.adminUserId == uid) {

                // Broadcast kill switch to offline devices
                val clearCommand = MeetupLocation(
                    pinId = "COMMAND_CLEAR_ALL",
                    partyId = party.partyId,
                    latitude = 0.0,
                    longitude = 0.0,
                    note = "COMMAND_CLEAR_ALL"
                )
                meshManager?.broadcastPin(clearCommand)

                // Admin wipes cloud database cleanly for everyone else
                try {
                    val pinsRef = firestore.collection("parties").document(party.partyId).collection("pins")
                    val snapshot = pinsRef.get().await()
                    val batch = firestore.batch()
                    for (doc in snapshot.documents) {
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- USER PROFILE & AUTH LOGIC ---

    fun isUserLoggedIn(): Boolean {
        return if (auth.currentUser != null) {
            initMeshEngine()
            loadUserParties()
            true
        } else {
            false
        }
    }

    fun logoutUser() {
        auth.signOut()
        turnOffOfflineMesh()
    }

    fun registerUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(user.email.trim(), user.password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userProfile = user.copy(userId = firebaseUser.uid, password = "")
                    firestore.collection("users").document(firebaseUser.uid).set(userProfile).await()

                    initMeshEngine()
                    loadUserParties()

                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(true) }
                } else { kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false) } }
            } catch (e: Exception) { e.printStackTrace(); kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false) } }
        }
    }

    fun authenticateUser(email: String, pass: String, onResult: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val docSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val fetchedUser = docSnapshot.toObject(User::class.java)

                    initMeshEngine()
                    loadUserParties()

                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(fetchedUser) }
                } else { kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
            } catch (e: Exception) { e.printStackTrace(); kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnOffOfflineMesh()
    }
}