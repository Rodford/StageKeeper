package com.example.stagekeeper.data

import androidx.room.*

// Defining my user database queries here
@Dao
interface UserDao {
    // Registering a new account
    @Insert
    fun insertUser(user: User): Long

    // Added COLLATE NOCASE
    @Query("SELECT * FROM users WHERE email = :email COLLATE NOCASE LIMIT 1")
    fun getUserByEmail(email: String): User?

    // Updating optional profile fields later
    @Update
    fun updateUser(user: User): Int
}