package com.example.stagekeeper

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagekeeper.data.ChatMessage
import com.example.stagekeeper.data.FriendRequest
import com.example.stagekeeper.data.PartyGroup
import com.example.stagekeeper.data.PartyInvite
import com.example.stagekeeper.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- INACTIVITY TIMER STATE ---
    private var inactivityJob: Job? = null
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    fun resetInactivityTimer() {
        inactivityJob?.cancel()

        // Only run the timer if the user is actually logged in
        if (auth.currentUser != null) {
            inactivityJob = viewModelScope.launch {
                delay(5 * 60 * 1000L) // 5 minutes in milliseconds
                _sessionExpired.value = true
            }
        }
    }

    fun clearSessionExpiredFlag() {
        _sessionExpired.value = false
    }

    // --- CHAT STATES ---
    private val _partyMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val partyMessages: StateFlow<List<ChatMessage>> = _partyMessages.asStateFlow()

    private val _dmMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val dmMessages: StateFlow<List<ChatMessage>> = _dmMessages.asStateFlow()

    private var partyChatListener: ListenerRegistration? = null
    private var dmChatListener: ListenerRegistration? = null

    private fun cacheEmergencyInfo(user: User) {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("em_contact", user.emergencyContact ?: "No contact provided.")
            .putString("em_medical", user.medicalInfo ?: "No medical information provided.")
            .apply()
    }

    fun updateUserProfile(displayName: String, phone: String, emergency: String, medical: String, photoUri: String, onComplete: (Boolean) -> Unit) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(
            displayName = displayName,
            phoneNumber = phone,
            emergencyContact = emergency,
            medicalInfo = medical,
            profilePhotoUri = photoUri.ifBlank { user.profilePhotoUri }
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("users").document(user.userId).set(updatedUser).await()
                _currentUser.value = updatedUser
                cacheEmergencyInfo(updatedUser)
                kotlinx.coroutines.withContext(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    // ==========================================
    // CHAT SYSTEM LOGIC
    // ==========================================

    fun sendPartyMessage(partyName: String, text: String) {
        val user = currentUser.value ?: return
        val party = _availableParties.value.find { it.partyName == partyName } ?: return

        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            senderId = user.userId,
            senderName = user.displayName,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("parties").document(party.partyId)
            .collection("messages").document(message.messageId)
            .set(message)
    }

    fun startListeningToPartyChat(partyName: String) {
        partyChatListener?.remove()
        val party = _availableParties.value.find { it.partyName == partyName } ?: return

        partyChatListener = firestore.collection("parties").document(party.partyId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                _partyMessages.value = messages
            }
    }

    private fun getDMThreadId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    fun sendDirectMessage(friendId: String, text: String) {
        val user = currentUser.value ?: return
        val threadId = getDMThreadId(user.userId, friendId)

        val message = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            senderId = user.userId,
            senderName = user.displayName,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        firestore.collection("direct_messages").document(threadId)
            .collection("messages").document(message.messageId)
            .set(message)
    }

    fun startListeningToDMs(friendId: String) {
        dmChatListener?.remove()
        val user = currentUser.value ?: return
        val threadId = getDMThreadId(user.userId, friendId)

        dmChatListener = firestore.collection("direct_messages").document(threadId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                _dmMessages.value = messages
            }
    }

    fun stopListeningToDMs() {
        dmChatListener?.remove()
        dmChatListener = null
        _dmMessages.value = emptyList()
    }


    // --- FRIENDS & INVITES STATE ---
    private val _friendsList = MutableStateFlow<List<User>>(emptyList())
    val friendsList: StateFlow<List<User>> = _friendsList

    // NEW: StateFlow for Friends of Friends
    private val _suggestedFriends = MutableStateFlow<List<User>>(emptyList())
    val suggestedFriends: StateFlow<List<User>> = _suggestedFriends

    private val _incomingInvites = MutableStateFlow<List<PartyInvite>>(emptyList())
    val incomingInvites: StateFlow<List<PartyInvite>> = _incomingInvites

    private val _incomingFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingFriendRequests: StateFlow<List<FriendRequest>> = _incomingFriendRequests

    private var inviteListener: ListenerRegistration? = null
    private var friendRequestListener: ListenerRegistration? = null

    fun sendFriendRequest(searchQuery: String, onResult: (Boolean, String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        val currentUserDoc = _currentUser.value ?: return
        val cleanQuery = searchQuery.trim()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usernameClean = cleanQuery.removePrefix("@").lowercase()
                var snapshot = firestore.collection("users").whereEqualTo("username", usernameClean).get().await()

                if (snapshot.isEmpty) {
                    snapshot = firestore.collection("users").whereEqualTo("phoneNumber", cleanQuery).get().await()
                }

                if (snapshot.isEmpty) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "User not found.") }
                    return@launch
                }

                val friendDoc = snapshot.documents.first()
                val friendUser = friendDoc.toObject(User::class.java)!!

                if (friendUser.userId == currentUid) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "You cannot add yourself.") }
                    return@launch
                }

                if (currentUserDoc.friends.contains(friendUser.userId)) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "Already friends with this user.") }
                    return@launch
                }

                if (friendUser.blockedUsers.contains(currentUid)) {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "Cannot send request to this user.") }
                    return@launch
                }

                val requestRef = firestore.collection("friend_requests").document()
                val request = FriendRequest(
                    requestId = requestRef.id,
                    fromUserId = currentUid,
                    fromUserName = currentUserDoc.displayName,
                    toUserId = friendUser.userId
                )
                requestRef.set(request).await()

                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    onResult(true, "Friend request sent to ${friendUser.displayName}!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false, "Network error.") }
            }
        }
    }

    fun respondToFriendRequest(request: FriendRequest, accept: Boolean) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("friend_requests").document(request.requestId).delete().await()

                if (accept) {
                    val batch = firestore.batch()
                    batch.update(firestore.collection("users").document(currentUid), "friends", FieldValue.arrayUnion(request.fromUserId))
                    batch.update(firestore.collection("users").document(request.fromUserId), "friends", FieldValue.arrayUnion(currentUid))
                    batch.commit().await()

                    val updatedMe = _currentUser.value?.copy(friends = _currentUser.value!!.friends + request.fromUserId)
                    _currentUser.value = updatedMe
                    loadFriendsList()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun removeFriend(friendId: String) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val batch = firestore.batch()
                batch.update(firestore.collection("users").document(currentUid), "friends", FieldValue.arrayRemove(friendId))
                batch.update(firestore.collection("users").document(friendId), "friends", FieldValue.arrayRemove(currentUid))
                batch.commit().await()

                val updatedMe = _currentUser.value?.copy(friends = _currentUser.value!!.friends.filter { it != friendId })
                _currentUser.value = updatedMe
                loadFriendsList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun blockUser(userIdToBlock: String) {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val batch = firestore.batch()
                batch.update(firestore.collection("users").document(currentUid), "blockedUsers", FieldValue.arrayUnion(userIdToBlock))
                batch.update(firestore.collection("users").document(currentUid), "friends", FieldValue.arrayRemove(userIdToBlock))
                batch.update(firestore.collection("users").document(userIdToBlock), "friends", FieldValue.arrayRemove(currentUid))
                batch.commit().await()

                val updatedMe = _currentUser.value?.copy(
                    friends = _currentUser.value!!.friends.filter { it != userIdToBlock },
                    blockedUsers = _currentUser.value!!.blockedUsers + userIdToBlock
                )
                _currentUser.value = updatedMe
                loadFriendsList()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // CHANGED: Now also computes mutual friends for suggestions!
    fun loadFriendsList() {
        val currentUid = auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userDoc = firestore.collection("users").document(currentUid).get().await()
                val currentUserObj = userDoc.toObject(User::class.java) ?: return@launch
                val friendIds = currentUserObj.friends

                if (friendIds.isEmpty()) {
                    _friendsList.value = emptyList()
                    _suggestedFriends.value = emptyList()
                    return@launch
                }

                val snapshot = firestore.collection("users")
                    .whereIn("userId", friendIds.take(10))
                    .get()
                    .await()

                val fetchedFriends = snapshot.toObjects(User::class.java)
                _friendsList.value = fetchedFriends

                // --- GENERATE MUTUAL FRIEND SUGGESTIONS ---
                val mutualCandidates = mutableSetOf<String>()
                fetchedFriends.forEach { friend ->
                    mutualCandidates.addAll(friend.friends)
                }

                // Filter out self, existing friends, and blocked users
                val filteredCandidates = mutualCandidates.filter { id ->
                    id != currentUid &&
                            !currentUserObj.friends.contains(id) &&
                            !currentUserObj.blockedUsers.contains(id)
                }.take(10) // Limit suggestions

                if (filteredCandidates.isNotEmpty()) {
                    val suggestionsSnapshot = firestore.collection("users")
                        .whereIn("userId", filteredCandidates)
                        .get()
                        .await()

                    // Filter out people who have blocked US
                    val validSuggestions = suggestionsSnapshot.toObjects(User::class.java).filter {
                        !it.blockedUsers.contains(currentUid)
                    }
                    _suggestedFriends.value = validSuggestions
                } else {
                    _suggestedFriends.value = emptyList()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendPartyInvite(friend: User, partyName: String, onResult: (Boolean) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        val currentDisplayName = _currentUser.value?.displayName ?: "A friend"
        val party = _availableParties.value.find { it.partyName == partyName } ?: return

        val inviteRef = firestore.collection("invites").document()
        val invite = PartyInvite(
            inviteId = inviteRef.id,
            fromUserId = currentUid,
            fromUserName = currentDisplayName,
            toUserId = friend.userId,
            partyId = party.partyId,
            partyName = party.partyName,
            inviteCode = party.inviteCode
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                inviteRef.set(invite).await()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun startListeningForInvites() {
        val currentUid = auth.currentUser?.uid ?: return
        inviteListener?.remove()
        friendRequestListener?.remove()

        inviteListener = firestore.collection("invites")
            .whereEqualTo("toUserId", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _incomingInvites.value = snapshot.toObjects(PartyInvite::class.java)
            }

        friendRequestListener = firestore.collection("friend_requests")
            .whereEqualTo("toUserId", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _incomingFriendRequests.value = snapshot.toObjects(FriendRequest::class.java)
            }
    }

    fun respondToInvite(invite: PartyInvite, accept: Boolean, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("invites").document(invite.inviteId).delete().await()

                if (accept) {
                    joinParty(invite.inviteCode) { success, _ ->
                        onComplete(success)
                    }
                } else {
                    kotlinx.coroutines.withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

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

        // 1. Bypass the lazy local cache and demand the real history from Google's servers
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

            // 2. Only broadcast the Kill Switch and wipe the Cloud if this user is the Admin!
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
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            initMeshEngine()
            loadUserParties()
            loadFriendsList()
            startListeningForInvites()

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val docSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val fetchedUser = docSnapshot.toObject(User::class.java)
                    if (fetchedUser != null) {
                        _currentUser.value = fetchedUser
                        cacheEmergencyInfo(fetchedUser)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
            true
        } else {
            false
        }
    }

    fun logoutUser() {
        auth.signOut()
        _currentUser.value = null
        inviteListener?.remove()
        friendRequestListener?.remove()
        partyChatListener?.remove()
        dmChatListener?.remove()
        turnOffOfflineMesh()

        inactivityJob?.cancel()
        _sessionExpired.value = false
    }

    fun registerUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(user.email.trim(), user.password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userProfile = user.copy(userId = firebaseUser.uid, password = "")
                    firestore.collection("users").document(firebaseUser.uid).set(userProfile).await()

                    _currentUser.value = userProfile
                    cacheEmergencyInfo(userProfile)

                    initMeshEngine()
                    loadUserParties()
                    loadFriendsList()
                    startListeningForInvites()

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

                    _currentUser.value = fetchedUser
                    if (fetchedUser != null) {
                        cacheEmergencyInfo(fetchedUser)
                    }

                    initMeshEngine()
                    loadUserParties()
                    loadFriendsList()
                    startListeningForInvites()

                    kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(fetchedUser) }
                } else { kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
            } catch (e: Exception) { e.printStackTrace(); kotlinx.coroutines.withContext(Dispatchers.Main) { onResult(null) } }
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnOffOfflineMesh()
        partyChatListener?.remove()
        dmChatListener?.remove()
    }
}