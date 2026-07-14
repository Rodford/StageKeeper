package com.example.stagekeeper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stagekeeper.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val locationDao = database.locationDao()
    private val userDao = database.userDao()

    // --- MAP PIN LOGIC ---

    // Exposes database state to the Compose UI to trigger automatic map redraws
    val allLocations: StateFlow<List<MeetupLocation>> = locationDao.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Offloads database insertion to a background thread to prevent UI freezing
    fun saveLocationToDatabase(lat: Double, lng: Double, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            locationDao.insertLocation(MeetupLocation(latitude = lat, longitude = lng, note = note))
        }
    }

    // Clears all custom map markers
    fun deleteAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            locationDao.deleteAll()
        }
    }

    // --- USER PROFILE & AUTH LOGIC ---

    // Safely writes a new user to the database on a background thread
    fun registerUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userDao.insertUser(user)
                // Switch back to the Main thread so the UI can navigate to the next screen safely
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    // Checks the database for matching credentials
    fun authenticateUser(email: String, pass: String, onResult: (User?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Added .trim() here to catch accidental spaces from mobile auto-complete
            val user = userDao.getUserByEmail(email.trim())

            withContext(Dispatchers.Main) {
                // If the user exists and the password matches, return the User object. Otherwise, return null.
                if (user != null && user.password == pass) {
                    onResult(user)
                } else {
                    onResult(null)
                }
            }
        }
    }
}