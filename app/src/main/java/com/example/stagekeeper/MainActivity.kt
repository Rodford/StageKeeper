package com.example.stagekeeper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import coil.compose.AsyncImage
import com.example.stagekeeper.data.PartyGroup
import com.example.stagekeeper.data.User
import com.google.android.gms.location.LocationServices
import com.mapbox.common.MapboxOptions
import com.mapbox.common.TileRegionLoadOptions
import com.mapbox.common.TileStore
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.OfflineManager
import com.mapbox.maps.Style
import com.mapbox.maps.TilesetDescriptorOptions
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
import java.io.File

enum class AppScreen { Splash, Login, SignUp, Setup, Map, Profile }

// Database of 100 major US music festivals with accurate venue coordinates
val festivalLocations = mapOf(
    "Rolling Loud (FL)" to Point.fromLngLat(-81.4026, 28.5383),
    "EDC Orlando (FL)" to Point.fromLngLat(-81.4026, 28.5383),
    "EDC Las Vegas (NV)" to Point.fromLngLat(-115.0103, 36.2723),
    "Welcome to Rockville (FL)" to Point.fromLngLat(-81.0705, 29.1852),
    "Coachella (CA)" to Point.fromLngLat(-116.2372, 33.6784),
    "Lollapalooza (IL)" to Point.fromLngLat(-87.6204, 41.8739),
    "Austin City Limits (TX)" to Point.fromLngLat(-97.7728, 30.2669),
    "Bonnaroo (TN)" to Point.fromLngLat(-86.0483, 35.4746),
    "Sonic Temple (OH)" to Point.fromLngLat(-82.9912, 40.0095),
    "Ultra Music Festival (FL)" to Point.fromLngLat(-80.1856, 25.7781),
    "Governors Ball (NY)" to Point.fromLngLat(-73.8643, 40.7550),
    "Electric Forest (MI)" to Point.fromLngLat(-86.3686, 43.8398),
    "Firefly (DE)" to Point.fromLngLat(-75.5268, 39.1897),
    "Outside Lands (CA)" to Point.fromLngLat(-122.4837, 37.7690),
    "Hangout Music Fest (AL)" to Point.fromLngLat(-87.6813, 30.2457),
    "Summerfest (WI)" to Point.fromLngLat(-87.9019, 43.0305),
    "Louder Than Life (KY)" to Point.fromLngLat(-85.7480, 38.2093),
    "BottleRock (CA)" to Point.fromLngLat(-122.2882, 38.2974),
    "Tortuga Music Festival (FL)" to Point.fromLngLat(-80.1035, 26.1158),
    "Shaky Knees (GA)" to Point.fromLngLat(-84.3755, 33.7663),
    "Day N Vegas (NV)" to Point.fromLngLat(-115.1728, 36.1147),
    "Forecastle (KY)" to Point.fromLngLat(-85.7423, 38.2560),
    "Life is Beautiful (NV)" to Point.fromLngLat(-115.1408, 36.1718),
    "Movement (MI)" to Point.fromLngLat(-83.0405, 42.3308),
    "HARD Summer (CA)" to Point.fromLngLat(-118.2613, 34.0116),
    "Rock on the Range (OH)" to Point.fromLngLat(-82.9912, 40.0095),
    "Made In America (PA)" to Point.fromLngLat(-75.1715, 39.9579),
    "Sea.Hear.Now (NJ)" to Point.fromLngLat(-74.0026, 40.2185),
    "Bourbon & Beyond (KY)" to Point.fromLngLat(-85.7480, 38.2093),
    "Innings Festival (AZ)" to Point.fromLngLat(-111.9365, 33.4300),
    "New Orleans Jazz Fest (LA)" to Point.fromLngLat(-90.0768, 29.9880),
    "Pitchfork Music Fest (IL)" to Point.fromLngLat(-87.6743, 41.8845),
    "Afropunk Brooklyn (NY)" to Point.fromLngLat(-73.9772, 40.6908),
    "Stagecoach (CA)" to Point.fromLngLat(-116.2372, 33.6784),
    "Electric Zoo (NY)" to Point.fromLngLat(-73.9238, 40.7967),
    "Just Like Heaven (CA)" to Point.fromLngLat(-118.2613, 34.0116),
    "Desert Daze (CA)" to Point.fromLngLat(-117.2289, 33.8821),
    "Voodoo Experience (LA)" to Point.fromLngLat(-90.0907, 29.9840),
    "Boston Calling (MA)" to Point.fromLngLat(-71.1306, 42.3663),
    "Music Midtown (GA)" to Point.fromLngLat(-84.3773, 33.7806),
    "Ohana Festival (CA)" to Point.fromLngLat(-117.6536, 33.4608),
    "Levitation (TX)" to Point.fromLngLat(-97.7431, 30.2672),
    "High Water Festival (SC)" to Point.fromLngLat(-79.9142, 32.8465),
    "Blue Ridge Rock Fest (VA)" to Point.fromLngLat(-79.0344, 36.6342),
    "Cruel World (CA)" to Point.fromLngLat(-118.2613, 34.0116),
    "Kilby Block Party (UT)" to Point.fromLngLat(-111.8906, 40.7608),
    "III Points (FL)" to Point.fromLngLat(-80.1983, 25.7941),
    "Reggae Rise Up (FL)" to Point.fromLngLat(-82.6403, 27.7712),
    "Wonderfront (CA)" to Point.fromLngLat(-117.1711, 32.7093),
    "Big Ears (TN)" to Point.fromLngLat(-83.9189, 35.9606),
    "Pickathon (OR)" to Point.fromLngLat(-122.5348, 45.4373),
    "Outside Lands (CA)" to Point.fromLngLat(-122.4837, 37.7690),
    "Desert Hearts (CA)" to Point.fromLngLat(-116.0350, 33.5650),
    "Dirtybird Campout (CA)" to Point.fromLngLat(-117.1260, 34.4286),
    "Hard Red Rocks (CO)" to Point.fromLngLat(-105.2057, 39.6654),
    "Lost Lands (OH)" to Point.fromLngLat(-82.3556, 39.9576),
    "Ubbi Dubbi (TX)" to Point.fromLngLat(-97.4357, 32.7483),
    "Project Glow (DC)" to Point.fromLngLat(-76.9749, 38.9056),
    "Imagine Music Festival (GA)" to Point.fromLngLat(-84.8197, 33.5855),
    "Something Wonderful (TX)" to Point.fromLngLat(-96.7970, 32.7767),
    "Moonrise Festival (MD)" to Point.fromLngLat(-76.6075, 39.2274),
    "Global Dance Festival (CO)" to Point.fromLngLat(-105.0063, 39.7437),
    "Sunset Music Festival (FL)" to Point.fromLngLat(-82.5029, 27.9750),
    "Elements Festival (PA)" to Point.fromLngLat(-75.5268, 39.1897),
    "Breakaway Festival (OH)" to Point.fromLngLat(-83.0007, 39.9612),
    "Skull and Roses (CA)" to Point.fromLngLat(-119.2726, 34.2805),
    "Darker Waves (CA)" to Point.fromLngLat(-118.4000, 33.8600),
    "Daytona 500 Fan Fest (FL)" to Point.fromLngLat(-81.0705, 29.1852),
    "BeachLife Festival (CA)" to Point.fromLngLat(-118.3965, 33.8407),
    "Just Like Heaven (CA)" to Point.fromLngLat(-118.2613, 34.0116),
    "Festival 8 (CA)" to Point.fromLngLat(-116.2372, 33.6784),
    "Vegas Golden Knights Fan Fest (NV)" to Point.fromLngLat(-115.1728, 36.1147),
    "Hulaween (FL)" to Point.fromLngLat(-82.9157, 30.3957),
    "Suwannee Rising (FL)" to Point.fromLngLat(-82.9157, 30.3957),
    "Wakaan Music Festival (AR)" to Point.fromLngLat(-93.8184, 35.4746),
    "Resonance (OH)" to Point.fromLngLat(-82.7214, 39.5600),
    "Summer Camp Music Fest (IL)" to Point.fromLngLat(-89.6582, 40.9168),
    "High Sierra Music Fest (CA)" to Point.fromLngLat(-120.5750, 39.8119),
    "FloydFest (VA)" to Point.fromLngLat(-80.3204, 36.9082),
    "Telluride Bluegrass (CO)" to Point.fromLngLat(-107.8115, 37.9375),
    "MerleFest (NC)" to Point.fromLngLat(-81.1610, 36.1437),
    "DelFest (MD)" to Point.fromLngLat(-78.9329, 39.6644),
    "Grey Fox Bluegrass (NY)" to Point.fromLngLat(-73.5350, 42.2706),
    "Hardly Strictly Bluegrass (CA)" to Point.fromLngLat(-122.4837, 37.7690),
    "Peach Music Festival (PA)" to Point.fromLngLat(-75.6514, 41.4443),
    "Camp Bisco (PA)" to Point.fromLngLat(-75.6514, 41.4443),
    "Jam on the River (PA)" to Point.fromLngLat(-75.1450, 39.9500),
    "4 Peaks Music Festival (OR)" to Point.fromLngLat(-121.2858, 44.0582),
    "WinterWonderGrass (CO)" to Point.fromLngLat(-106.8175, 39.4649),
    "Gem and Jam (AZ)" to Point.fromLngLat(-110.9747, 32.2226),
    "Arizona Roots (AZ)" to Point.fromLngLat(-111.9365, 33.4300),
    "Reggae Rise Up Vegas (NV)" to Point.fromLngLat(-115.1398, 36.1699),
    " Cali Vibes (CA)" to Point.fromLngLat(-118.1937, 33.7701),
    "Wonderfront (CA)" to Point.fromLngLat(-117.1711, 32.7093),
    "Innings Festival Florida (FL)" to Point.fromLngLat(-80.1918, 25.7617),
    "Sunfest (FL)" to Point.fromLngLat(-80.0533, 26.7153),
    "Rocklahoma (OK)" to Point.fromLngLat(-95.2755, 36.5615),
    "Inkcarceration (OH)" to Point.fromLngLat(-82.5186, 40.7593),
    "Adjacent Music Festival (NJ)" to Point.fromLngLat(-74.4217, 39.3643)
)

class MainActivity : ComponentActivity() {

    private val mapViewModel: MapViewModel by viewModels()

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == false) {
            Toast.makeText(this, "GPS required to save locations", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken =
            "pk.eyJ1Ijoicm9kZm9yZDM3IiwiYSI6ImNtcWk1aGk3bDAzNnYycnB3YW9vaGhhMm0ifQ.ia5rsvhyqD1oMsNwGvZ5tQ"

        val requiredPermissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
        requestPermissionsLauncher.launch(requiredPermissions.toTypedArray())

        setContent { StageKeeperAppNavigation(mapViewModel) }
    }

    // Requests coordinates from hardware sensors and delegates data to the ViewModel
    @SuppressLint("MissingPermission")
    fun grabHardwareLocationAndSave(note: String, activeParty: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                mapViewModel.saveLocationToDatabase(
                    location.latitude,
                    location.longitude,
                    note,
                    activeParty
                )
                Toast.makeText(this, "Pin Dropped!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Searching for GPS signal...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Kicks off a silent background download for a specific festival area
    fun cacheFestivalMapLocally(festivalName: String) {
        val point = festivalLocations[festivalName] ?: return

        try {
            val offlineManager = OfflineManager()
            val tileStore = TileStore.create()

            val tilesetDescriptor = offlineManager.createTilesetDescriptor(
                TilesetDescriptorOptions.Builder().styleURI(Style.MAPBOX_STREETS).minZoom(14)
                    .maxZoom(17).build()
            )
            val minLat = point.latitude() - 0.05
            val minLng = point.longitude() - 0.05
            val maxLat = point.latitude() + 0.05
            val maxLng = point.longitude() + 0.05
            val bounds = Polygon.fromLngLats(
                listOf(
                    listOf(
                        Point.fromLngLat(minLng, minLat),
                        Point.fromLngLat(maxLng, minLat),
                        Point.fromLngLat(maxLng, maxLat),
                        Point.fromLngLat(minLng, maxLat),
                        Point.fromLngLat(minLng, minLat)
                    )
                )
            )

            tileStore.loadTileRegion(
                "festival_cache_$festivalName",
                TileRegionLoadOptions.Builder().geometry(bounds)
                    .descriptors(listOf(tilesetDescriptor)).build(),
                { },
                { })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun StageKeeperAppNavigation(viewModel: MapViewModel) {
    // State to track which screen we are currently viewing
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

    // Keeping these globally so the map screen knows exactly what festival and party the user picked
    var userParty by remember { mutableStateOf("Select Party") }
    var userFestival by remember { mutableStateOf("Select Festival") }

    val availableParties by viewModel.availableParties.collectAsState()

    when (currentScreen) {
        AppScreen.Splash -> SplashScreen(onSplashComplete = {
            if (viewModel.isUserLoggedIn()) {
                currentScreen = AppScreen.Setup
            } else {
                currentScreen = AppScreen.Login
            }
        })
        AppScreen.Login -> LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { currentScreen = AppScreen.Setup },
            onNavigateToSignUp = { currentScreen = AppScreen.SignUp })

        AppScreen.SignUp -> SignUpScreen(
            viewModel = viewModel,
            onSignUpSuccess = { currentScreen = AppScreen.Login },
            onBackToLogin = { currentScreen = AppScreen.Login })

        AppScreen.Setup -> SetupScreen(
            selectedParty = userParty,
            onPartySelected = {
                userParty = it
                viewModel.startListeningToPartyPins(it)
                viewModel.turnOnOfflineMesh()
            },
            selectedFestival = userFestival, onFestivalSelected = { userFestival = it },
            availableParties = availableParties, viewModel = viewModel,
            onLaunchMap = { currentScreen = AppScreen.Map },
            onNavigateProfile = { currentScreen = AppScreen.Profile }
        )

        AppScreen.Map -> MainMapScreen(
            viewModel = viewModel,
            activeParty = userParty,
            onPartyChange = {
                userParty = it
                viewModel.startListeningToPartyPins(it)
                viewModel.turnOnOfflineMesh()
            },
            activeFestival = userFestival,
            onFestivalChange = { userFestival = it },
            availableParties = availableParties,
            onNavigateHome = { currentScreen = AppScreen.Setup },
            onNavigateProfile = { currentScreen = AppScreen.Profile }
        )

        AppScreen.Profile -> ProfileScreen(
            viewModel = viewModel,
            onNavigateBack = { currentScreen = AppScreen.Setup },
            onLogout = { currentScreen = AppScreen.Login }
        )
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val splashBackground = Color.Black;
    val stageKeeperPurple = Color(0xFFA644FF)
    LaunchedEffect(Unit) { delay(2500); onSplashComplete() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(splashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stretching the logo out to fill the full width of the screen
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "StageKeeper Logo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Just a fake loading spinner to make it look professional while booting up
            CircularProgressIndicator(
                color = stageKeeperPurple,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MapViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current;
    val focusManager = LocalFocusManager.current
    val stageKeeperDark = Color(0xFF050505);
    val stageKeeperPurple = Color(0xFFA644FF);
    val stageKeeperBlue = Color(0xFF00BFFF)
    var email by remember { mutableStateOf("") };
    var password by remember { mutableStateOf("") }

    var showEmergencyDialog by remember { mutableStateOf(false) }

    val attemptLogin = {
        focusManager.clearFocus()
        if (email.isNotBlank() && password.isNotBlank()) {
            viewModel.authenticateUser(email, password) { user ->
                if (user != null) {
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showEmergencyDialog) {
        val sharedPrefs = context.getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
        val emContact = sharedPrefs.getString("em_contact", "No contact provided.")
        val emMedical = sharedPrefs.getString("em_medical", "No medical info provided.")

        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color.Red) },
            title = { Text("Emergency Information") },
            text = {
                Column {
                    Text("Emergency Contact:", fontWeight = FontWeight.Bold, color = stageKeeperPurple)
                    Text(emContact ?: "", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Medical Info:", fontWeight = FontWeight.Bold, color = stageKeeperPurple)
                    Text(emMedical ?: "", color = Color.White)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEmergencyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Close", color = Color.White) }
            },
            containerColor = Color(0xFF1A1A1A),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "StageKeeper",
            color = stageKeeperPurple,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        ); Text(
        "Find your crew.",
        color = stageKeeperBlue,
        fontSize = 16.sp
    ); Spacer(modifier = Modifier.height(48.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        ); Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.LightGray) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { attemptLogin() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        ); Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { attemptLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text(
                "Login",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }; Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onNavigateToSignUp) {
            Text(
                "Don't have an account? Sign Up",
                color = stageKeeperBlue
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showEmergencyDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF330000)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Emergency Info", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: MapViewModel, onSignUpSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    val context = LocalContext.current;
    val stageKeeperDark = Color(0xFF050505);
    val stageKeeperPurple = Color(0xFFA644FF)
    var email by remember { mutableStateOf("") };
    var password by remember { mutableStateOf("") };
    var username by remember { mutableStateOf("") };
    var displayName by remember { mutableStateOf("") };
    var phone by remember { mutableStateOf("") };
    var emergencyContact by remember { mutableStateOf("") };
    var medicalInfo by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            TextButton(onClick = onBackToLogin) {
                Text(
                    "Back to Login",
                    color = stageKeeperPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }; Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Create Account",
            color = stageKeeperPurple,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        ); Text("Join the party securely.", color = Color.LightGray, fontSize = 16.sp); Spacer(
        modifier = Modifier.height(32.dp)
    )
        Text(
            "Required Info",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        ); Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Safety & Festival Details (Optional)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        ); Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = emergencyContact,
            onValueChange = { emergencyContact = it },
            label = { Text("Emergency Contact Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = medicalInfo,
            onValueChange = { medicalInfo = it },
            label = { Text("Medical Info") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        ); Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                val newUser = User(
                    email = email,
                    password = password,
                    username = username,
                    displayName = displayName,
                    phoneNumber = phone.ifBlank { null },
                    emergencyContact = emergencyContact.ifBlank { null },
                    medicalInfo = medicalInfo.ifBlank { null },
                    partyCode = ""
                )

                viewModel.registerUser(newUser) { success ->
                    if (success) {
                        Toast.makeText(
                            context,
                            "Account Created! Please Log In.",
                            Toast.LENGTH_LONG
                        ).show(); onSignUpSuccess()
                    } else {
                        Toast.makeText(context, "Error creating account.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && username.isNotBlank() && displayName.isNotBlank()
        ) {
            Text(
                "Complete Sign Up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }; Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    selectedParty: String,
    onPartySelected: (String) -> Unit,
    selectedFestival: String,
    onFestivalSelected: (String) -> Unit,
    availableParties: List<PartyGroup>,
    viewModel: MapViewModel,
    onLaunchMap: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current;
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val stageKeeperDark = Color(0xFF050505);
    val stageKeeperPurple = Color(0xFFA644FF);
    val stageKeeperBlue = Color(0xFF00BFFF)
    var partyExpanded by remember { mutableStateOf(false) };
    var festivalExpanded by remember { mutableStateOf(false) };
    val festivals = festivalLocations.keys.toList()
    var showCreatePartyDialog by remember { mutableStateOf(false) };
    var newPartyName by remember { mutableStateOf("") };
    var showJoinPartyDialog by remember { mutableStateOf(false) };
    var joinInviteCode by remember { mutableStateOf("") }

    // Dialog states for Friends system
    var showFriendsDashboard by remember { mutableStateOf(false) }
    var friendSearchQuery by remember { mutableStateOf("") }
    var showInviteFriendsDialog by remember { mutableStateOf(false) }

    val incomingInvites by viewModel.incomingInvites.collectAsState()

    // INCOMING INVITE LISTENER DIALOG
    if (incomingInvites.isNotEmpty()) {
        val invite = incomingInvites.first()
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Crew Invite!", color = stageKeeperPurple, fontWeight = FontWeight.Bold) },
            text = { Text("${invite.fromUserName} invited you to join ${invite.partyName}.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.respondToInvite(invite, true) { success ->
                            if (success) {
                                onPartySelected(invite.partyName)
                                Toast.makeText(context, "Joined ${invite.partyName}!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Accept", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.respondToInvite(invite, false) {} }) {
                    Text("Decline", color = Color.Red)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    if (showFriendsDashboard) {
        val friends by viewModel.friendsList.collectAsState()
        val friendRequests by viewModel.incomingFriendRequests.collectAsState()
        val suggestedFriends by viewModel.suggestedFriends.collectAsState() // NEW Mutual Friends

        androidx.compose.ui.window.Dialog(onDismissRequest = { showFriendsDashboard = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Friends Dashboard", color = stageKeeperPurple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1: Add a Friend
                    Text("Add New Friend", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = friendSearchQuery,
                            onValueChange = { friendSearchQuery = it },
                            placeholder = { Text("@username or Phone", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (friendSearchQuery.isNotBlank()) {
                                    viewModel.sendFriendRequest(friendSearchQuery) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        if (success) friendSearchQuery = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                        ) { Text("Send") }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SECTION 2: Pending Requests
                    if (friendRequests.isNotEmpty()) {
                        Text("Pending Requests (${friendRequests.size})", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp).fillMaxWidth()) {
                            items(friendRequests) { request ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(request.fromUserName, color = Color.White)
                                    Row {
                                        TextButton(onClick = { viewModel.respondToFriendRequest(request, false) }) {
                                            Text("Decline", color = Color.Red, fontSize = 12.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.respondToFriendRequest(request, true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) { Text("Accept", fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 3: Suggested Friends (Mutuals)
                    if (suggestedFriends.isNotEmpty()) {
                        Text("Suggested Friends", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp).fillMaxWidth()) {
                            items(suggestedFriends) { suggested ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(suggested.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("@${suggested.username}", color = stageKeeperBlue, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.sendFriendRequest("@${suggested.username}") { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) { Text("Add", fontSize = 12.sp, color = Color.White) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 4: My Friends List
                    Text("My Friends", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (friends.isEmpty()) {
                        Text("No friends added yet.", color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(friends) { friend ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(friend.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("@${friend.username}", color = stageKeeperBlue, fontSize = 12.sp)
                                    }
                                    Row {
                                        TextButton(onClick = { viewModel.removeFriend(friend.userId) }) {
                                            Text("Remove", color = Color.LightGray, fontSize = 12.sp)
                                        }
                                        TextButton(onClick = { viewModel.blockUser(friend.userId) }) {
                                            Text("Block", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showFriendsDashboard = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) { Text("Close", color = Color.White) }
                }
            }
        }
    }

    if (showInviteFriendsDialog) {
        val friends by viewModel.friendsList.collectAsState()

        AlertDialog(
            onDismissRequest = { showInviteFriendsDialog = false },
            title = { Text("Invite to $selectedParty", color = stageKeeperPurple, fontWeight = FontWeight.Bold) },
            text = {
                if (friends.isEmpty()) {
                    Text("No friends added yet. Add friends by their @username to invite them directly!", color = Color.White)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)) {
                        items(friends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(friend.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("@${friend.username}", color = stageKeeperBlue, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.sendPartyInvite(friend, selectedParty) { success ->
                                            Toast.makeText(context, if (success) "Invite sent to ${friend.displayName}!" else "Failed to send invite", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Invite", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInviteFriendsDialog = false }) {
                    Text("Close", color = stageKeeperPurple)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    if (showCreatePartyDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePartyDialog = false },
            title = { Text("Create New Crew") },
            text = {
                OutlinedTextField(
                    value = newPartyName,
                    onValueChange = { newPartyName = it },
                    label = { Text("Crew Name") })
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPartyName.isNotBlank()) {
                            viewModel.createNewParty(newPartyName) { inviteCode ->
                                Toast.makeText(
                                    context,
                                    "Crew Created!",
                                    Toast.LENGTH_SHORT
                                ).show(); onPartySelected(newPartyName); showCreatePartyDialog =
                                false; newPartyName = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Create", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePartyDialog = false
                }) { Text("Cancel", color = stageKeeperPurple) }
            })
    }
    if (showJoinPartyDialog) {
        AlertDialog(
            onDismissRequest = { showJoinPartyDialog = false },
            title = { Text("Join a Crew") },
            text = {
                OutlinedTextField(
                    value = joinInviteCode,
                    onValueChange = { joinInviteCode = it },
                    label = { Text("6-Digit Invite Code") })
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinInviteCode.isNotBlank()) {
                            viewModel.joinParty(joinInviteCode) { success, resultMessage ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Joined $resultMessage!",
                                        Toast.LENGTH_SHORT
                                    ).show(); onPartySelected(resultMessage); showJoinPartyDialog =
                                        false; joinInviteCode = ""
                                } else {
                                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Join", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showJoinPartyDialog = false }) {
                    Text(
                        "Cancel",
                        color = stageKeeperPurple
                    )
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { showFriendsDashboard = true }) {
                Text("Friends Dashboard", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onNavigateProfile) {
                Text(
                    "Profile",
                    color = stageKeeperPurple,
                    fontWeight = FontWeight.Bold
                )
            }
        }; Spacer(modifier = Modifier.height(48.dp))
        Text(
            "StageKeeper",
            color = stageKeeperPurple,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        ); Text(
        "Setup Your Event",
        color = Color.White,
        fontSize = 18.sp
    ); Spacer(modifier = Modifier.height(48.dp))

        // Dropdown 1: Picking the party/crew
        ExposedDropdownMenuBox(
            expanded = partyExpanded,
            onExpandedChange = { partyExpanded = !partyExpanded }) {
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
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedBorderColor = stageKeeperPurple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = partyExpanded,
                onDismissRequest = { partyExpanded = false }) {
                DropdownMenuItem(text = {
                    Text(
                        "🔗 Join Crew with Code",
                        color = stageKeeperBlue,
                        fontWeight = FontWeight.Bold
                    )
                }, onClick = { partyExpanded = false; showJoinPartyDialog = true })
                DropdownMenuItem(text = {
                    Text(
                        "➕ Create New Crew",
                        color = stageKeeperPurple,
                        fontWeight = FontWeight.Bold
                    )
                }, onClick = { partyExpanded = false; showCreatePartyDialog = true })
                availableParties.forEach { party ->
                    DropdownMenuItem(
                        text = { Text(party.partyName) },
                        onClick = { onPartySelected(party.partyName); partyExpanded = false })
                }
            }
        }

        val activePartyObj = availableParties.find { it.partyName == selectedParty }
        if (activePartyObj != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Invite Code: ${activePartyObj.inviteCode}",
                        color = stageKeeperBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activePartyObj.inviteCode))
                            Toast.makeText(context, "Code Copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                    ) { Text("Copy", color = Color.LightGray, fontSize = 14.sp) }

                    // Native Android Share Intent
                    TextButton(
                        onClick = { showInviteFriendsDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Invite", color = stageKeeperPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
                TextButton(onClick = {
                    viewModel.leaveParty(selectedParty) { success ->
                        if (success) {
                            onPartySelected("Select Party")
                        }
                    }
                }) { Text("Leave Crew", color = Color.Red, fontSize = 14.sp) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dropdown 2: Picking the specific festival
        ExposedDropdownMenuBox(
            expanded = festivalExpanded,
            onExpandedChange = { festivalExpanded = !festivalExpanded }) {
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
                    focusedContainerColor = Color(0xFF1A1A1A),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedBorderColor = stageKeeperPurple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(
                expanded = festivalExpanded,
                onDismissRequest = { festivalExpanded = false }) {
                festivals.forEach { festival ->
                    DropdownMenuItem(
                        text = { Text(festival) },
                        onClick = {
                            onFestivalSelected(festival); festivalExpanded =
                            false; (context as MainActivity).cacheFestivalMapLocally(festival); Toast.makeText(
                            context,
                            "Caching map for $festival...",
                            Toast.LENGTH_SHORT
                        ).show()
                        })
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = { onLaunchMap() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = selectedParty != "Select Party" && selectedFestival != "Select Festival"
        ) { Text("Enter Map", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
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
    availableParties: List<PartyGroup>,
    onNavigateHome: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val locations by viewModel.allLocations.collectAsState()

    val activePartyId = availableParties.find { it.partyName == activeParty }?.partyId ?: ""
    val activePartyLocations = locations.filter { it.partyId == activePartyId }

    val stageKeeperPurple = Color(0xFFA644FF);
    val stageKeeperBlue = Color(0xFF00BFFF);
    val stageKeeperDark = Color(0xFF050505)

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) };
    val redDotBitmap = remember { createSimpleRedDot() };
    var currentRenderedFestival by remember { mutableStateOf("") }

    // Dialog States
    var showNoteDialog by remember { mutableStateOf(false) };
    var currentNoteText by remember { mutableStateOf("") };
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var partyExpanded by remember { mutableStateOf(false) };
    var festivalExpanded by remember { mutableStateOf(false) };
    val festivals = festivalLocations.keys.toList()
    var showCreatePartyDialog by remember { mutableStateOf(false) };
    var newPartyName by remember { mutableStateOf("") };
    var showJoinPartyDialog by remember { mutableStateOf(false) };
    var joinInviteCode by remember { mutableStateOf("") }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Add a Note") },
            text = {
                OutlinedTextField(
                    value = currentNoteText,
                    onValueChange = { currentNoteText = it },
                    label = { Text("e.g., Meetup spot") })
            },
            confirmButton = {
                Button(onClick = {
                    showNoteDialog = false; (context as MainActivity).grabHardwareLocationAndSave(
                    currentNoteText,
                    activeParty
                ); currentNoteText = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)) {
                    Text(
                        "Save Pin"
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text(
                        "Cancel",
                        color = stageKeeperPurple
                    )
                }
            })
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Pins?") },
            text = { Text("This will remove all meetup pins for this crew. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        // Pass activeParty to the viewModel so it knows if you are the Admin
                        viewModel.deleteAllLocations(activeParty)
                        Toast.makeText(context, "All pins cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Clear", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = stageKeeperPurple)
                }
            }
        )
    }

    if (showCreatePartyDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePartyDialog = false },
            title = { Text("Create New Crew") },
            text = {
                OutlinedTextField(
                    value = newPartyName,
                    onValueChange = { newPartyName = it },
                    label = { Text("Crew Name") })
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPartyName.isNotBlank()) {
                            viewModel.createNewParty(newPartyName) { inviteCode ->
                                Toast.makeText(
                                    context,
                                    "Crew Created!",
                                    Toast.LENGTH_SHORT
                                ).show(); onPartyChange(newPartyName); showCreatePartyDialog =
                                false; newPartyName = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Create", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePartyDialog = false
                }) { Text("Cancel", color = stageKeeperPurple) }
            })
    }
    if (showJoinPartyDialog) {
        AlertDialog(
            onDismissRequest = { showJoinPartyDialog = false },
            title = { Text("Join a Crew") },
            text = {
                OutlinedTextField(
                    value = joinInviteCode,
                    onValueChange = { joinInviteCode = it },
                    label = { Text("6-Digit Invite Code") })
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinInviteCode.isNotBlank()) {
                            viewModel.joinParty(joinInviteCode) { success, resultMessage ->
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Joined $resultMessage!",
                                        Toast.LENGTH_SHORT
                                    ).show(); onPartyChange(resultMessage); showJoinPartyDialog =
                                        false; joinInviteCode = ""
                                } else {
                                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Join", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showJoinPartyDialog = false }) {
                    Text(
                        "Cancel",
                        color = stageKeeperPurple
                    )
                }
            })
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(stageKeeperDark)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(stageKeeperDark)
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Home Button & Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateHome) {
                    Text(
                        "Home",
                        color = stageKeeperPurple,
                        fontWeight = FontWeight.Bold
                    )
                }; Text(
                "StageKeeper",
                color = stageKeeperPurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ); TextButton(onClick = onNavigateProfile) {
                Text(
                    "Profile",
                    color = stageKeeperPurple,
                    fontWeight = FontWeight.Bold
                )
            }
            }; Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = partyExpanded,
                        onExpandedChange = { partyExpanded = !partyExpanded }) {
                        OutlinedTextField(
                            value = activeParty,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Party", color = Color.LightGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedBorderColor = stageKeeperPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = partyExpanded,
                            onDismissRequest = { partyExpanded = false }) {
                            DropdownMenuItem(text = {
                                Text(
                                    "🔗 Join Crew with Code",
                                    color = stageKeeperBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }, onClick = { partyExpanded = false; showJoinPartyDialog = true })
                            DropdownMenuItem(text = {
                                Text(
                                    "➕ Create New Crew",
                                    color = stageKeeperPurple,
                                    fontWeight = FontWeight.Bold
                                )
                            }, onClick = { partyExpanded = false; showCreatePartyDialog = true })
                            availableParties.forEach { party ->
                                DropdownMenuItem(
                                    text = { Text(party.partyName) },
                                    onClick = {
                                        onPartyChange(party.partyName); partyExpanded = false
                                    })
                            }
                        }
                    }
                    val activePartyObj = availableParties.find { it.partyName == activeParty }
                    if (activePartyObj != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Code: ${activePartyObj.inviteCode}",
                                    color = stageKeeperBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activePartyObj.inviteCode))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp).padding(start = 8.dp)
                                ) { Text("Copy", color = Color.LightGray, fontSize = 12.sp) }
                            }
                            TextButton(
                                onClick = {
                                    viewModel.leaveParty(activeParty) { success ->
                                        if (success) {
                                            onPartyChange("Select Party")
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) { Text("Leave", color = Color.Red, fontSize = 12.sp) }
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
                        label = { Text("Festival", color = Color.LightGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = stageKeeperPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = festivalExpanded,
                        onDismissRequest = { festivalExpanded = false }) {
                        festivals.forEach { festival ->
                            DropdownMenuItem(
                                text = { Text(festival) },
                                onClick = { onFestivalChange(festival); festivalExpanded = false })
                        }
                    }
                }
            }
        }

        // MAPBOX VIEW
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    val themedContext = ContextThemeWrapper(
                        ctx,
                        androidx.appcompat.R.style.Theme_AppCompat_DayNight
                    )
                    MapView(themedContext).apply {
                        compass.enabled = false; logo.enabled = false; attribution.enabled =
                        false; mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                        style.addImage(
                            "red_dot",
                            redDotBitmap
                        )
                    }; annotationManager = annotations.createPointAnnotationManager()
                    }
                },
                update = { view ->

                    // --- CAMERA & FESTIVAL LOGIC ---
                    if (currentRenderedFestival != activeFestival) {
                        currentRenderedFestival = activeFestival

                        if (activeFestival == "Select Festival") {
                            // Default back to following the user's physical GPS
                            view.location.enabled = true; view.viewport.transitionTo(
                                view.viewport.makeFollowPuckViewportState(
                                    FollowPuckViewportStateOptions.Builder().zoom(16.0).build()
                                )
                            )
                        } else {

                            view.location.enabled =
                                true; view.viewport.idle(); festivalLocations[activeFestival]?.let { point ->
                                view.mapboxMap.setCamera(
                                    CameraOptions.Builder().center(point).zoom(14.5).build()
                                )
                            }
                        }
                    }

                    // --- PIN RENDERING LOGIC ---
                    annotationManager?.let { manager ->
                        manager.deleteAll();
                        val optionsList = activePartyLocations.map { loc ->
                            PointAnnotationOptions().withPoint(
                                Point.fromLngLat(
                                    loc.longitude,
                                    loc.latitude
                                )
                            ).withIconImage("red_dot").withTextField(loc.note)
                                .withTextOffset(listOf(0.0, 1.5)).withTextColor(AndroidColor.BLACK)
                        }; manager.create(optionsList)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // BOTTOM ACTION BUTTONS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(stageKeeperDark)
                .navigationBarsPadding()
                .padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showNoteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Drop Pin", fontWeight = FontWeight.Bold, color = Color.White) }
            Button(
                onClick = { showClearConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Clear Pins", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MapViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)

    val user by viewModel.currentUser.collectAsState()

    var isEditing by remember { mutableStateOf(false) }

    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phoneNumber ?: "") }
    var emergencyContact by remember(user) { mutableStateOf(user?.emergencyContact ?: "") }
    var medicalInfo by remember(user) { mutableStateOf(user?.medicalInfo ?: "") }
    var photoUri by remember(user) { mutableStateOf(user?.profilePhotoUri ?: "") }

    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val permanentUri = context.copyUriToPermanentFile(uri)
            photoUri = permanentUri.toString()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            val permanentUri = context.copyUriToPermanentFile(tempCameraUri!!)
            photoUri = permanentUri.toString()
        }
    }

    if (showPhotoOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            title = { Text("Profile Photo", color = stageKeeperPurple, fontWeight = FontWeight.Bold) },
            text = { Text("Choose where to get your picture from.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        showPhotoOptionsDialog = false
                        tempCameraUri = context.createImageFileUri()
                        cameraLauncher.launch(tempCameraUri!!)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
                ) { Text("Take Photo", color = Color.White) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showPhotoOptionsDialog = false
                        imagePicker.launch("image/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) { Text("Gallery", color = Color.White) }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("Back", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = {
                viewModel.logoutUser()
                onLogout()
            }) {
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable(enabled = isEditing) { showPhotoOptionsDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri.isNotBlank()) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Placeholder",
                        tint = Color.LightGray,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (isEditing) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    "Your Profile",
                    color = stageKeeperPurple,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("@${user?.username ?: "user"}", color = stageKeeperBlue, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isEditing) {
            Text("General Info", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it },
                label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Emergency & Medical (Visible on Login)", color = Color.Red, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = emergencyContact, onValueChange = { emergencyContact = it },
                label = { Text("Emergency Contact") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = medicalInfo, onValueChange = { medicalInfo = it },
                label = { Text("Medical Info") }, modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    viewModel.updateUserProfile(displayName, phone, emergencyContact, medicalInfo, photoUri) { success ->
                        if (success) {
                            isEditing = false
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
            ) { Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold) }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Display Name", color = stageKeeperBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(user?.displayName ?: "N/A", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Phone Number", color = stageKeeperBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(user?.phoneNumber ?: "N/A", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Emergency Contact", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(user?.emergencyContact ?: "None provided", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Medical Info", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(user?.medicalInfo ?: "None provided", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) { Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

fun createSimpleRedDot(): Bitmap {
    val size = 40;
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888);
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.RED; style = Paint.Style.FILL; isAntiAlias = true
    }; canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    return bitmap
}

fun Context.createImageFileUri(): Uri {
    val imagePath = File(cacheDir, "images").apply { mkdirs() }
    val tempFile = File.createTempFile("profile_", ".jpg", imagePath)
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", tempFile)
}

fun Context.copyUriToPermanentFile(sourceUri: Uri): Uri {
    val destinationFile = File(filesDir, "profile_${System.currentTimeMillis()}.jpg")
    contentResolver.openInputStream(sourceUri)?.use { inputStream ->
        destinationFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return Uri.fromFile(destinationFile)
}