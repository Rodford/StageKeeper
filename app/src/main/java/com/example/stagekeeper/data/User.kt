package com.example.stagekeeper.data

data class User(
    val userId: String = "",
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