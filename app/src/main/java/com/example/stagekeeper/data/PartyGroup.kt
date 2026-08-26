package com.example.stagekeeper.data

data class PartyGroup(
    val partyId: String = "",
    val partyName: String = "",
    val inviteCode: String = "",
    val adminUserId: String = "",
    val memberIds: List<String> = emptyList(),
    val activeFestival: String = ""
)