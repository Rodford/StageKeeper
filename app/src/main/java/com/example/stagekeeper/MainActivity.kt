package com.example.stagekeeper

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.gms.location.LocationServices
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport

class MainActivity : ComponentActivity() {

    private val mapViewModel: MapViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "GPS required to save locations", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapboxOptions.accessToken = "pk.eyJ1Ijoicm9kZm9yZDM3IiwiYSI6ImNtcWk1aGk3bDAzNnYycnB3YW9vaGhhMm0ifQ.ia5rsvhyqD1oMsNwGvZ5tQ"

        // Request location permissions on startup
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            StageKeeperApp(mapViewModel)
        }
    }

    // Requests coordinates from hardware sensors and delegates data to the ViewModel
    @SuppressLint("MissingPermission")
    fun grabHardwareLocationAndSave(note: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mapViewModel.saveLocationToDatabase(location.latitude, location.longitude, note)
                Toast.makeText(this, "Location Saved to Database!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Searching for GPS signal...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun StageKeeperApp(viewModel: MapViewModel) {
    val context = LocalContext.current
    val locations by viewModel.allLocations.collectAsState()

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val redDotBitmap = remember { createSimpleRedDot() }

    var showNoteDialog by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }

    // Custom Marker Input Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add a Note") },
            text = {
                OutlinedTextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    label = { Text("e.g., Main Stage, Bathrooms, etc.") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    showNoteDialog = false
                    (context as MainActivity).grabHardwareLocationAndSave(currentNoteText)
                    currentNoteText = ""
                }) {
                    Text("Save Pin")
                }
            },
            dismissButton = {
                Button(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    ) {
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showNoteDialog = true },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Add Note & Pin")
            }

            Button(
                onClick = { viewModel.deleteAllLocations() },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.DarkGray),
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Clear All Pins")
            }
        }

        // Mapbox OpenGL View
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val themedContext = ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat_DayNight)

                MapView(themedContext).apply {
                    compass.enabled = false
                    logo.enabled = false
                    attribution.enabled = false

                    mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                        // Cache the image to prevent main-thread ANR crashes
                        style.addImage("red_dot", redDotBitmap)
                    }

                    // Activate live location tracking puck and camera follow
                    location.enabled = true
                    viewport.transitionTo(
                        targetState = viewport.makeFollowPuckViewportState(
                            FollowPuckViewportStateOptions.Builder()
                                .zoom(16.0)
                                .build()
                        )
                    )

                    annotationManager = annotations.createPointAnnotationManager()
                }
            },
            update = { view ->
                annotationManager?.let { manager ->
                    manager.deleteAll()

                    // Batch compile annotations to prevent rendering lag
                    val optionsList = locations.map { loc ->
                        PointAnnotationOptions()
                            .withPoint(Point.fromLngLat(loc.longitude, loc.latitude))
                            .withIconImage("red_dot")
                            .withTextField(loc.note)
                            .withTextOffset(listOf(0.0, 1.5))
                            .withTextColor(Color.BLACK)
                    }

                    manager.create(optionsList)

                    // Snap camera to the most recently added custom pin
                    if (locations.isNotEmpty()) {
                        val latestLocation = locations.last()
                        view.mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(latestLocation.longitude, latestLocation.latitude))
                                .zoom(16.0)
                                .build()
                        )
                    }
                }
            }
        )
    }
}

// Graphic generator for custom Mapbox annotations
fun createSimpleRedDot(): Bitmap {
    val size = 40
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bitmap
}