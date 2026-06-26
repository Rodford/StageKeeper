package com.example.stagekeeper

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay

// Define our three app screens
enum class AppScreen {
    Splash,
    Setup,
    Map
}

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
            StageKeeperAppNavigation(mapViewModel)
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
fun StageKeeperAppNavigation(viewModel: MapViewModel) {
    // State to track which screen we are currently viewing
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

    // Global state for user's selections to pass to the Map screen
    var userParty by remember { mutableStateOf("Select Party") }
    var userFestival by remember { mutableStateOf("Select Festival") }

    // Navigation Switcher
    when (currentScreen) {
        AppScreen.Splash -> {
            SplashScreen(onSplashComplete = { currentScreen = AppScreen.Setup })
        }
        AppScreen.Setup -> {
            SetupScreen(
                selectedParty = userParty,
                onPartySelected = { userParty = it },
                selectedFestival = userFestival,
                onFestivalSelected = { userFestival = it },
                onLaunchMap = { currentScreen = AppScreen.Map },
                onResetHome = {
                    userParty = "Select Party"
                    userFestival = "Select Festival"
                }
            )
        }
        AppScreen.Map -> {
            MainMapScreen(
                viewModel = viewModel,
                activeParty = userParty,
                onPartyChange = { userParty = it },
                activeFestival = userFestival,
                onFestivalChange = { userFestival = it },
                onNavigateHome = { currentScreen = AppScreen.Setup }
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val stageKeeperDark = Color(0xFF1E1E1E)
    val stageKeeperPurple = Color(0xFFA644FF)

    LaunchedEffect(Unit) {
        delay(2000)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "StageKeeper",
                color = stageKeeperPurple,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(color = stageKeeperPurple)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    selectedParty: String,
    onPartySelected: (String) -> Unit,
    selectedFestival: String,
    onFestivalSelected: (String) -> Unit,
    onLaunchMap: () -> Unit,
    onResetHome: () -> Unit
) {
    val stageKeeperDark = Color(0xFF1E1E1E)
    val stageKeeperPurple = Color(0xFFA644FF)

    var partyExpanded by remember { mutableStateOf(false) }
    val parties = listOf("Rockville Crew", "EDC Group", "Rolling Loud Party")

    var festivalExpanded by remember { mutableStateOf(false) }
    val festivals = listOf("Welcome to Rockville", "EDC Orlando", "Rolling Loud")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Reset (Home) Button for the Setup Screen
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(onClick = onResetHome) {
                Text("Home / Reset", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "StageKeeper",
            color = stageKeeperPurple,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Setup Your Event",
            color = Color.White,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 1. SELECT PARTY
        ExposedDropdownMenuBox(
            expanded = partyExpanded,
            onExpandedChange = { partyExpanded = !partyExpanded }
        ) {
            OutlinedTextField(
                value = selectedParty,
                onValueChange = {},
                readOnly = true,
                label = { Text("1. Select Party", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = stageKeeperPurple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AndroidColor.BLACK.let { Color(it) },
                    unfocusedTextColor = AndroidColor.BLACK.let { Color(it) }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = partyExpanded,
                onDismissRequest = { partyExpanded = false }
            ) {
                parties.forEach { party ->
                    DropdownMenuItem(
                        text = { Text(party) },
                        onClick = {
                            onPartySelected(party)
                            partyExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. SELECT FESTIVAL
        ExposedDropdownMenuBox(
            expanded = festivalExpanded,
            onExpandedChange = { festivalExpanded = !festivalExpanded }
        ) {
            OutlinedTextField(
                value = selectedFestival,
                onValueChange = {},
                readOnly = true,
                label = { Text("2. Select Festival", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = stageKeeperPurple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = AndroidColor.BLACK.let { Color(it) },
                    unfocusedTextColor = AndroidColor.BLACK.let { Color(it) }
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = festivalExpanded,
                onDismissRequest = { festivalExpanded = false }
            ) {
                festivals.forEach { festival ->
                    DropdownMenuItem(
                        text = { Text(festival) },
                        onClick = {
                            onFestivalSelected(festival)
                            festivalExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 3. LAUNCH MAP BUTTON
        Button(
            onClick = { onLaunchMap() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = selectedParty != "Select Party" && selectedFestival != "Select Festival"
        ) {
            Text("Enter Map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMapScreen(
    viewModel: MapViewModel,
    activeParty: String,
    onPartyChange: (String) -> Unit,
    activeFestival: String,
    onFestivalChange: (String) -> Unit,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val locations by viewModel.allLocations.collectAsState()

    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperDark = Color(0xFF1E1E1E)

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val redDotBitmap = remember { createSimpleRedDot() }

    var showNoteDialog by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }

    // Dropdown states for Map Screen
    var partyExpanded by remember { mutableStateOf(false) }
    val parties = listOf("Rockville Crew", "EDC Group", "Rolling Loud Party")

    var festivalExpanded by remember { mutableStateOf(false) }
    val festivals = listOf("Welcome to Rockville", "EDC Orlando", "Rolling Loud")

    // Tracks the last applied festival so the camera doesn't violently snap while placing pins
    var currentRenderedFestival by remember { mutableStateOf("") }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add a Note") },
            text = {
                OutlinedTextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    label = { Text("e.g., Meetup spot, Main Stage, etc.") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoteDialog = false
                        (context as MainActivity).grabHardwareLocationAndSave(currentNoteText)
                        currentNoteText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) {
                    Text("Save Pin")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel", color = stageKeeperPurple) }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
    ) {
        // TOP APP BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(stageKeeperPurple)
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Home Button & Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateHome) {
                    Text("Home", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "StageKeeper",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Spacer to balance the layout against the Home button
                Spacer(modifier = Modifier.width(64.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DROPDOWN ROW (Change Party & Change Festival Side-by-Side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Change Party Dropdown
                ExposedDropdownMenuBox(
                    expanded = partyExpanded,
                    onExpandedChange = { partyExpanded = !partyExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = activeParty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Party") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = partyExpanded,
                        onDismissRequest = { partyExpanded = false }
                    ) {
                        parties.forEach { party ->
                            DropdownMenuItem(
                                text = { Text(party) },
                                onClick = {
                                    onPartyChange(party)
                                    partyExpanded = false
                                }
                            )
                        }
                    }
                }

                // Change Festival Dropdown
                ExposedDropdownMenuBox(
                    expanded = festivalExpanded,
                    onExpandedChange = { festivalExpanded = !festivalExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = activeFestival,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Festival") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = festivalExpanded,
                        onDismissRequest = { festivalExpanded = false }
                    ) {
                        festivals.forEach { festival ->
                            DropdownMenuItem(
                                text = { Text(festival) },
                                onClick = {
                                    onFestivalChange(festival)
                                    festivalExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // MAPBOX VIEW
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val themedContext = ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat_DayNight)

                    MapView(themedContext).apply {
                        compass.enabled = false
                        logo.enabled = false
                        attribution.enabled = false

                        mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                            style.addImage("red_dot", redDotBitmap)
                        }

                        annotationManager = annotations.createPointAnnotationManager()
                    }
                },
                update = { view ->

                    // --- CAMERA & FESTIVAL LOGIC ---
                    if (currentRenderedFestival != activeFestival) {
                        currentRenderedFestival = activeFestival

                        if (activeFestival == "Select Festival") {
                            // Default back to following the user's physical GPS
                            view.location.enabled = true
                            view.viewport.transitionTo(
                                view.viewport.makeFollowPuckViewportState(
                                    FollowPuckViewportStateOptions.Builder()
                                        .zoom(16.0)
                                        .build()
                                )
                            )
                        } else {
                            // A specific festival was chosen. Turn location on, but idle the viewport
                            view.location.enabled = true
                            view.viewport.idle()

                            // Map the festival name to the correct real-world coordinates
                            val targetPoint = when (activeFestival) {
                                "Welcome to Rockville" -> Point.fromLngLat(-81.0705, 29.1852) // Daytona International Speedway
                                "EDC Orlando", "Rolling Loud" -> Point.fromLngLat(-81.4026, 28.5383) // Camping World Stadium
                                else -> null
                            }

                            // Snap the camera to the specific festival grounds
                            targetPoint?.let { point ->
                                view.mapboxMap.setCamera(
                                    CameraOptions.Builder()
                                        .center(point)
                                        .zoom(14.5)
                                        .build()
                                )
                            }
                        }
                    }

                    // --- PIN RENDERING LOGIC ---
                    annotationManager?.let { manager ->
                        manager.deleteAll()

                        val optionsList = locations.map { loc ->
                            PointAnnotationOptions()
                                .withPoint(Point.fromLngLat(loc.longitude, loc.latitude))
                                .withIconImage("red_dot")
                                .withTextField(loc.note)
                                .withTextOffset(listOf(0.0, 1.5))
                                .withTextColor(AndroidColor.BLACK)
                        }
                        manager.create(optionsList)
                    }
                }
            )
        }

        // BOTTOM ACTION BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(stageKeeperDark)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showNoteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Drop Pin", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { viewModel.deleteAllLocations() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear Pins", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// Graphic generator for custom Mapbox annotations
fun createSimpleRedDot(): Bitmap {
    val size = 40
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bitmap
}