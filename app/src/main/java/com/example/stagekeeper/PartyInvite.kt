package com.example.stagekeeper.data

data class PartyInvite(
    val inviteId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val partyId: String = "",
    val partyName: String = "",
    val inviteCode: String = "",
    val timestamp: Long = System.currentTimeMillis()
)