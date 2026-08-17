package com.example.stagekeeper.data

data class FriendRequest(
    val requestId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)