package com.example.stagekeeper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Blueprint for user profiles so I can hold onto emails, passwords, and optional emergency info
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val email: String,
    val password: String,
    val username: String,
    val displayName: String,
    val phoneNumber: String?,
    val emergencyContact: String?,
    val medicalInfo: String?,
    val partyCode: String
)