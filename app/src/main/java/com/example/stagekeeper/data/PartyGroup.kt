package com.example.stagekeeper.data

// Blueprint for cloud-based party groups
data class PartyGroup(
    val partyId: String = "",
    val partyName: String = "",
    val inviteCode: String = "",    // <-- The 6-digit code friends will type in
    val adminUserId: String = "",
    val memberIds: List<String> = emptyList(),
    val activeFestival: String = ""
)