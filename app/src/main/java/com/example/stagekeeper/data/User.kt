package com.example.stagekeeper.data

// Blueprint for user profiles, now configured for Firebase Firestore
data class User(
    val userId: String = "",       // Changed to String to hold the Firebase UID
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val displayName: String = "",
    val phoneNumber: String? = null,
    val emergencyContact: String? = null,
    val medicalInfo: String? = null,
    val partyCode: String = "",
    val profilePhotoUri: String? = null,
    val friends: List<String> = emptyList(),
    val blockedUsers: List<String> = emptyList()
)