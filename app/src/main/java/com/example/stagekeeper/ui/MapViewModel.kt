package com.example.stagekeeper

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).locationDao()

    // Exposes database state to the Compose UI to trigger automatic map redraws
    val allLocations: StateFlow<List<MeetupLocation>> = dao.getAllLocations()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Offloads database insertion to a background thread to prevent UI freezing
    fun saveLocationToDatabase(lat: Double, lng: Double, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertLocation(MeetupLocation(latitude = lat, longitude = lng, note = note))
        }
    }

    // Clears all custom map markers
    fun deleteAllLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAll()
        }
    }
}