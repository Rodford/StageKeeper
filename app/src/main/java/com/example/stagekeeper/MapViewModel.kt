package com.example.stagekeeper

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.stagekeeper.data.ChatMessage
import com.example.stagekeeper.data.FriendRequest
import com.example.stagekeeper.data.PartyGroup
import com.example.stagekeeper.data.PartyInvite
import com.example.stagekeeper.data.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

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
import kotlinx.coroutines.withContext

import java.util.UUID

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val locationDao = database.locationDao()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var meshManager: MeshManager? = null

    // --- STATE FLOWS ---
    private val _availableParties = MutableStateFlow<List<PartyGroup>>(emptyList())
    val availableParties: StateFlow<List<PartyGroup>> = _availableParties

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isLowPowerMode = MutableStateFlow(false)
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    private val _partyMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val partyMessages: StateFlow<List<ChatMessage>> = _partyMessages.asStateFlow()

    private val _dmMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val dmMessages: StateFlow<List<ChatMessage>> = _dmMessages.asStateFlow()

    private val _friendsList = MutableStateFlow<List<User>>(emptyList())
    val friendsList: StateFlow<List<User>> = _friendsList

    private val _suggestedFriends = MutableStateFlow<List<User>>(emptyList())
    val suggestedFriends: StateFlow<List<User>> = _suggestedFriends

    private val _incomingInvites = MutableStateFlow<List<PartyInvite>>(emptyList())
    val incomingInvites: StateFlow<List<PartyInvite>> = _incomingInvites

    private val _incomingFriendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingFriendRequests: StateFlow<List<FriendRequest>> = _incomingFriendRequests

    val allLocations: StateFlow<List<MeetupLocation>> = locationDao.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.Companion.Lazily, emptyList())

    // --- LISTENERS & JOBS ---
    private var inactivityJob: Job? = null
    private var partyChatListener: ListenerRegistration? = null
    private var dmChatListener: ListenerRegistration? = null
    private var inviteListener: ListenerRegistration? = null
    private var friendRequestListener: ListenerRegistration? = null
    private var pinListener: ListenerRegistration? = null

    // ==========================================
    // SYSTEM & POWER CONTROLS
    // ==========================================
    fun togglePowerMode(enabled: Boolean) {
        _isLowPowerMode.value = enabled
    }

    fun resetInactivityTimer() {
        inactivityJob?.cancel()
        if (auth.currentUser != null) {
            inactivityJob = viewModelScope.launch {
                delay(5 * 60 * 1000L)
                _sessionExpired.value = true
            }
        }
    }

    fun clearSessionExpiredFlag() {
        _sessionExpired.value = false
    }

    // ==========================================
    // USER PROFILE
    // ==========================================
    private fun cacheEmergencyInfo(user: User) {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("em_contact", user.emergencyContact ?: "No contact provided.")
            putString("em_medical", user.medicalInfo ?: "No medical information provided.")
        }
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
                withContext(Dispatchers.Main) { onComplete(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    // ==========================================
    // CHAT SYSTEM
    // ==========================================
    fun sendPartyMessage(partyName: String, text: String) {
        val user = currentUser.value ?: return
        val party = _availableParties.value.find { it.partyName == partyName } ?: return
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val message = ChatMessage(messageId = msgId, senderId = user.userId, senderName = user.displayName, text = text, timestamp = timestamp)
        val payload = "P_CHAT|${msgId}|${user.userId}|${user.displayName}|${party.partyId}|$text|$timestamp"

        meshManager?.broadcastData(payload)

        val currentList = _partyMessages.value.toMutableList()
        currentList.add(message)
        _partyMessages.value = currentList.sortedBy { it.timestamp }

        firestore.collection("parties").document(party.partyId).collection("messages").document(message.messageId).set(message)
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

                val currentIds = _partyMessages.value.map { it.messageId }.toSet()
                val newMessages = messages.filter { !currentIds.contains(it.messageId) }

                if (newMessages.isNotEmpty()) {
                    val combined = (_partyMessages.value + newMessages).sortedBy { it.timestamp }.distinctBy { it.messageId }
                    _partyMessages.value = combined
                }
            }
    }

    private fun getDMThreadId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) "${userId1}_${userId2}" else "${userId2}_${userId1}"
    }

    fun sendDirectMessage(friendId: String, text: String) {
        val user = currentUser.value ?: return
        val threadId = getDMThreadId(user.userId, friendId)
        val msgId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val message = ChatMessage(messageId = msgId, senderId = user.userId, senderName = user.displayName, text = text, timestamp = timestamp)
        val payload = "D_CHAT|${msgId}|${user.userId}|${user.displayName}|$threadId|$text|$timestamp"

        meshManager?.broadcastData(payload)

        val currentList = _dmMessages.value.toMutableList()
        currentList.add(message)
        _dmMessages.value = currentList.sortedBy { it.timestamp }

        firestore.collection("direct_messages").document(threadId).collection("messages").document(message.messageId).set(message)
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

                val currentIds = _dmMessages.value.map { it.messageId }.toSet()
                val newMessages = messages.filter { !currentIds.contains(it.messageId) }

                if (newMessages.isNotEmpty()) {
                    val combined = (_dmMessages.value + newMessages).sortedBy { it.timestamp }.distinctBy { it.messageId }
                    _dmMessages.value = combined
                }
            }
    }

    fun stopListeningToDMs() {
        dmChatListener?.remove()
        dmChatListener = null
        _dmMessages.value = emptyList()
    }

    // ==========================================
    // FRIENDS & INVITES
    // ==========================================
    fun sendFriendRequest(searchQuery: String, onResult: (Boolean, String) -> Unit) {
        val currentUid = auth.currentUser?.uid ?: return
        val currentUserDoc = _currentUser.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanQuery = searchQuery.trim().removePrefix("@")
                var snapshot = firestore.collection("users").whereEqualTo("username", cleanQuery.lowercase()).get().await()

                if (snapshot.isEmpty) snapshot = firestore.collection("users").whereEqualTo("username", cleanQuery).get().await()
                if (snapshot.isEmpty) snapshot = firestore.collection("users").whereEqualTo("phoneNumber", cleanQuery).get().await()

                if (snapshot.isEmpty) {
                    withContext(Dispatchers.Main) { onResult(false, "User not found.") }
                    return@launch
                }

                val friendDoc = snapshot.documents.first()
                val friendUser = friendDoc.toObject(User::class.java)!!

                if (friendUser.userId == currentUid) {
                    withContext(Dispatchers.Main) { onResult(false, "You cannot add yourself.") }
                    return@launch
                }

                if (currentUserDoc.friends.contains(friendUser.userId)) {
                    withContext(Dispatchers.Main) { onResult(false, "Already friends with this user.") }
                    return@launch
                }

                if (friendUser.blockedUsers.contains(currentUid)) {
                    withContext(Dispatchers.Main) { onResult(false, "Cannot send request to this user.") }
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

                withContext(Dispatchers.Main) {
                    onResult(true, "Friend request sent to ${friendUser.displayName}!")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, "Network error.") }
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

                val snapshot = firestore.collection("users").whereIn("userId", friendIds.take(10)).get().await()
                val fetchedFriends = snapshot.toObjects(User::class.java)
                _friendsList.value = fetchedFriends

                val mutualCandidates = mutableSetOf<String>()
                fetchedFriends.forEach { friend -> mutualCandidates.addAll(friend.friends) }

                val filteredCandidates = mutualCandidates.filter { id ->
                    id != currentUid && !currentUserObj.friends.contains(id) && !currentUserObj.blockedUsers.contains(id)
                }.take(10)

                if (filteredCandidates.isNotEmpty()) {
                    val suggestionsSnapshot = firestore.collection("users").whereIn("userId", filteredCandidates).get().await()
                    val validSuggestions = suggestionsSnapshot.toObjects(User::class.java).filter { !it.blockedUsers.contains(currentUid) }
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
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun startListeningForInvites() {
        val currentUid = auth.currentUser?.uid ?: return
        inviteListener?.remove()
        friendRequestListener?.remove()

        inviteListener = firestore.collection("invites").whereEqualTo("toUserId", currentUid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _incomingInvites.value = snapshot.toObjects(PartyInvite::class.java)
            }

        friendRequestListener = firestore.collection("friend_requests").whereEqualTo("toUserId", currentUid)
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
                    joinParty(invite.inviteCode) { success, _ -> onComplete(success) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(true) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    // ==========================================
    // MESH NETWORK & PINS
    // ==========================================
    private fun getHiddenPins(): Set<String> {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("hidden_pins", emptySet()) ?: emptySet()
    }

    private fun addHiddenPins(pinIds: List<String>) {
        val prefs = getApplication<Application>().getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("hidden_pins", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.addAll(pinIds)
        prefs.edit { putStringSet("hidden_pins", current) }
    }

    private fun initMeshEngine() {
        val uid = auth.currentUser?.uid ?: return
        meshManager = MeshManager(getApplication(), uid) { rawPayload ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val parts = rawPayload.split("|")
                    when (parts[0]) {
                        "PIN" -> {
                            val pin = MeetupLocation(
                                pinId = parts[1], partyId = parts[2],
                                latitude = parts[3].toDouble(), longitude = parts[4].toDouble(), note = parts[5]
                            )
                            if (pin.pinId == "COMMAND_CLEAR_ALL") {
                                val currentPins = allLocations.value.map { it.pinId }
                                addHiddenPins(currentPins)
                                locationDao.deleteAll()
                            } else if (!getHiddenPins().contains(pin.pinId)) {
                                locationDao.insertLocation(pin)
                            }
                        }
                        "P_CHAT" -> {
                            val msg = ChatMessage(
                                messageId = parts[1], senderId = parts[2], senderName = parts[3],
                                text = parts[5], timestamp = parts[6].toLong()
                            )
                            val currentList = _partyMessages.value.toMutableList()
                            if (currentList.none { it.messageId == msg.messageId }) {
                                currentList.add(msg)
                                _partyMessages.value = currentList.sortedBy { it.timestamp }
                            }
                        }
                        "D_CHAT" -> {
                            val msg = ChatMessage(
                                messageId = parts[1], senderId = parts[2], senderName = parts[3],
                                text = parts[5], timestamp = parts[6].toLong()
                            )
                            val currentList = _dmMessages.value.toMutableList()
                            if (currentList.none { it.messageId == msg.messageId }) {
                                currentList.add(msg)
                                _dmMessages.value = currentList.sortedBy { it.timestamp }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }
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

    fun createNewParty(partyName: String, onResult: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val inviteCode = UUID.randomUUID().toString().take(6).uppercase()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newPartyRef = firestore.collection("parties").document()
                val newParty = PartyGroup(partyId = newPartyRef.id, partyName = partyName, inviteCode = inviteCode, adminUserId = uid, memberIds = listOf(uid))
                newPartyRef.set(newParty).await()
                loadUserParties()
                withContext(Dispatchers.Main) { onResult(inviteCode) }
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
                    withContext(Dispatchers.Main) { onResult(false, "Invalid Invite Code.") }
                    return@launch
                }
                val partyDoc = snapshot.documents.first()
                firestore.collection("parties").document(partyDoc.id).update("memberIds", FieldValue.arrayUnion(uid)).await()
                loadUserParties()
                withContext(Dispatchers.Main) { onResult(true, partyDoc.getString("partyName") ?: "") }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onResult(false, "Network error.") }
            }
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
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false) }
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

    fun startListeningToPartyPins(partyName: String) {
        pinListener?.remove()
        val party = _availableParties.value.find { it.partyName == partyName } ?: return
        val pinsRef = firestore.collection("parties").document(party.partyId).collection("pins")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val serverSnapshot = pinsRef.get(Source.SERVER).await()
                val hidden = getHiddenPins()
                serverSnapshot.toObjects(MeetupLocation::class.java).forEach { pin ->
                    if (!hidden.contains(pin.pinId)) locationDao.insertLocation(pin)
                }
            } catch (_: Exception) {
                Log.d("CloudSync", "Offline. Relying on local cache and Mesh Network.")
            }
        }

        pinListener = pinsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            viewModelScope.launch(Dispatchers.IO) {
                val hidden = getHiddenPins()
                val cloudPins = snapshot.toObjects(MeetupLocation::class.java)
                cloudPins.forEach { pin ->
                    if (!hidden.contains(pin.pinId)) locationDao.insertLocation(pin)
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
            val payload = "PIN|$pinId|$partyId|$lat|$lng|$note"
            meshManager?.broadcastData(payload)

            if (partyId.isNotBlank()) {
                firestore.collection("parties").document(partyId).collection("pins").document(pinId).set(loc)
            }
        }
    }

    fun deleteAllLocations(activePartyName: String) {
        val uid = auth.currentUser?.uid ?: return
        val party = _availableParties.value.find { it.partyName == activePartyName } ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val currentPins = allLocations.value.map { it.pinId }
            addHiddenPins(currentPins)
            locationDao.deleteAll()

            if (party.adminUserId == uid) {
                val clearCommand = "PIN|COMMAND_CLEAR_ALL|${party.partyId}|0.0|0.0|COMMAND_CLEAR_ALL"
                meshManager?.broadcastData(clearCommand)
                try {
                    val pinsRef = firestore.collection("parties").document(party.partyId).collection("pins")
                    val snapshot = pinsRef.get().await()
                    val batch = firestore.batch()
                    for (doc in snapshot.documents) batch.delete(doc.reference)
                    batch.commit().await()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // ==========================================
    // AUTHENTICATION & RECOVERY
    // ==========================================
    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, "Password reset email sent!")
                else onResult(false, task.exception?.message ?: "Error sending reset email.")
            }
    }

    fun recoverEmail(usernameOrPhone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cleanQuery = usernameOrPhone.trim().removePrefix("@")
                var snapshot = firestore.collection("users").whereEqualTo("username", cleanQuery.lowercase()).get().await()

                if (snapshot.isEmpty) snapshot = firestore.collection("users").whereEqualTo("username", cleanQuery).get().await()
                if (snapshot.isEmpty) snapshot = firestore.collection("users").whereEqualTo("phoneNumber", cleanQuery).get().await()

                if (!snapshot.isEmpty) {
                    val user = snapshot.documents.first().toObject(User::class.java)
                    if (user != null) {
                        val emailParts = user.email.split("@")
                        val maskedEmail = if (emailParts.size == 2) "${emailParts[0].first()}***@${emailParts[1]}" else user.email
                        withContext(Dispatchers.Main) { onResult(true, "Account found! Email: $maskedEmail") }
                        return@launch
                    }
                }
                withContext(Dispatchers.Main) { onResult(false, "No account found matching that info.") }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onResult(false, "Network error.") }
            }
        }
    }

    fun deleteAccount(onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        val uid = user?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("users").document(uid).delete().await()
                user.delete().await()
                withContext(Dispatchers.Main) {
                    logoutUser()
                    onComplete(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun checkAuthStatus(onResult: (isLoggedIn: Boolean, missingProfile: Boolean) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val docSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                    if (docSnapshot.exists()) {
                        val fetchedUser = docSnapshot.toObject(User::class.java)
                        if (fetchedUser != null) {
                            _currentUser.value = fetchedUser
                            cacheEmergencyInfo(fetchedUser)
                        }
                        initMeshEngine()
                        loadUserParties()
                        loadFriendsList()
                        startListeningForInvites()

                        withContext(Dispatchers.Main) { onResult(true, false) }
                    } else {
                        withContext(Dispatchers.Main) { onResult(true, true) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) { onResult(false, false) }
                }
            }
        } else {
            onResult(false, false)
        }
    }

    fun authenticateWithGoogle(idToken: String, onResult: (Boolean, Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val userDocRef = firestore.collection("users").document(uid)
                    val snapshot = userDocRef.get().await()

                    if (snapshot.exists()) {
                        val existingUser = snapshot.toObject(User::class.java)
                        _currentUser.value = existingUser
                        if (existingUser != null) cacheEmergencyInfo(existingUser)

                        initMeshEngine()
                        loadUserParties()
                        loadFriendsList()
                        startListeningForInvites()

                        withContext(Dispatchers.Main) { onResult(true, false, "Welcome back!") }
                    } else {
                        withContext(Dispatchers.Main) { onResult(true, true, "Please complete your profile.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, false, "Google Sign-In failed.") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, false, e.message ?: "Network Error") }
            }
        }
    }

    fun completeGoogleProfile(username: String, displayName: String, phone: String, emergencyContact: String, medicalInfo: String, onResult: (Boolean, String) -> Unit) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            onResult(false, "Authentication lost. Please log in again.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newUser = User(
                    userId = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    password = "",
                    username = username.trim().lowercase().removePrefix("@"),
                    displayName = displayName.trim(),
                    phoneNumber = phone.ifBlank { null },
                    emergencyContact = emergencyContact.ifBlank { null },
                    medicalInfo = medicalInfo.ifBlank { null },
                    partyCode = ""
                )

                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                _currentUser.value = newUser
                cacheEmergencyInfo(newUser)

                initMeshEngine()
                loadUserParties()
                loadFriendsList()
                startListeningForInvites()

                withContext(Dispatchers.Main) { onResult(true, "Profile completed!") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, e.message ?: "Error saving profile.") }
            }
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

                    withContext(Dispatchers.Main) { onResult(true) }
                } else {
                    withContext(Dispatchers.Main) { onResult(false) }
                }
            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { onResult(false) }
            }
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
                    if (fetchedUser != null) cacheEmergencyInfo(fetchedUser)

                    initMeshEngine()
                    loadUserParties()
                    loadFriendsList()
                    startListeningForInvites()

                    withContext(Dispatchers.Main) { onResult(fetchedUser) }
                } else {
                    withContext(Dispatchers.Main) { onResult(null) }
                }
            } catch (e: Exception) { e.printStackTrace(); withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        turnOffOfflineMesh()
        partyChatListener?.remove()
        dmChatListener?.remove()
    }
}