package com.example.stagekeeper

import android.app.Application
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

    // NEW: Our Mesh Network Controller
    private var meshManager: MeshManager? = null

    private val _availableParties = MutableStateFlow<List<PartyGroup>>(emptyList())
    val availableParties: StateFlow<List<PartyGroup>> = _availableParties

    // --- MESH NETWORK CONTROLS ---

    private fun initMeshEngine() {
        val uid = auth.currentUser?.uid ?: return
        meshManager = MeshManager(getApplication(), uid)
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

                // Automatically turn off the radios if we leave the crew!
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

        pinListener = firestore.collection("parties").document(party.partyId).collection("pins")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                viewModelScope.launch(Dispatchers.IO) {
                    val cloudPins = snapshot.toObjects(MeetupLocation::class.java)
                    cloudPins.forEach { pin -> locationDao.insertLocation(pin) }
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
            if (partyId.isNotBlank()) {
                try {
                    firestore.collection("parties").document(partyId).collection("pins").document(pinId).set(loc)
                } catch (e: Exception) { /* Stay in Room if offline */ }
            }
        }
    }

    // Clears all custom map markers
    fun deleteAllLocations() {
        viewModelScope.launch(Dispatchers.IO) { locationDao.deleteAll() }
    }

    // --- USER PROFILE & AUTH LOGIC ---
    // --- Firebase Setup ---


    // Checks the database for matching credentials
    fun authenticateUser(email: String, pass: String, onResult: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val docSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val fetchedUser = docSnapshot.toObject(User::class.java)

                    initMeshEngine() // Wake up the engine!
                    loadUserParties()

                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(fetchedUser) }
                } else { kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
            } catch (e: Exception) { e.printStackTrace(); kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
        }
    }
}