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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.core.graphics.createBitmap
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

// The five main screens for the StageKeeper app
enum class AppScreen {
    Splash,
    Login,
    SignUp,
    Setup,
    Map
}

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

    // Kicks off a silent background download for a specific festival area
    fun cacheFestivalMapLocally(festivalName: String) {
        val point = festivalLocations[festivalName] ?: return

        try {
            val offlineManager = OfflineManager()
            val tileStore = TileStore.create()

            val tilesetDescriptor = offlineManager.createTilesetDescriptor(
                TilesetDescriptorOptions.Builder()
                    .styleURI(Style.MAPBOX_STREETS)
                    .minZoom(14)
                    .maxZoom(17)
                    .build()
            )

            // Creating a rough 3-mile bounding box around the center coordinate
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
                TileRegionLoadOptions.Builder()
                    .geometry(bounds)
                    .descriptors(listOf(tilesetDescriptor))
                    .build(),
                { progress ->
                    // Silent progress callback
                },
                { expected ->
                    // Silent completion callback
                }
            )

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

    // Navigation Switcher
    when (currentScreen) {
        AppScreen.Splash -> {
            SplashScreen(onSplashComplete = { currentScreen = AppScreen.Login })
        }
        AppScreen.Login -> {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { currentScreen = AppScreen.Setup },
                onNavigateToSignUp = { currentScreen = AppScreen.SignUp }
            )
        }
        AppScreen.SignUp -> {
            SignUpScreen(
                viewModel = viewModel,
                onSignUpSuccess = { currentScreen = AppScreen.Login },
                onBackToLogin = { currentScreen = AppScreen.Login }
            )
        }
        AppScreen.Setup -> {
            SetupScreen(
                selectedParty = userParty,
                onPartySelected = { userParty = it },
                selectedFestival = userFestival,
                onFestivalSelected = { userFestival = it },
                onLaunchMap = { currentScreen = AppScreen.Map },
                onLogout = { currentScreen = AppScreen.Login }
            )
        }
        AppScreen.Map -> {
            MainMapScreen(
                viewModel = viewModel,
                activeParty = userParty,
                onPartyChange = { userParty = it },
                activeFestival = userFestival,
                onFestivalChange = { userFestival = it },
                onNavigateHome = { currentScreen = AppScreen.Setup },
                onLogout = { currentScreen = AppScreen.Login }
            )
        }
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    // Using true black so the square edges of my JPG logo blend in and don't look like garbage
    val splashBackground = Color.Black
    val stageKeeperPurple = Color(0xFFA644FF)

    LaunchedEffect(Unit) {
        // Hanging on the splash screen for 2 and a half seconds so the spinner actually has time to do its thing
        delay(2500)
        onSplashComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(splashBackground),
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
                color = stageKeeperPurple, strokeWidth = 4.dp, modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MapViewModel, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("StageKeeper", color = stageKeeperPurple, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text("Find your crew.", color = stageKeeperBlue, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Color.LightGray) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.LightGray) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { attemptLogin() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { attemptLogin() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignUp) {
            Text("Don't have an account? Sign Up", color = stageKeeperBlue)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: MapViewModel, onSignUpSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            TextButton(onClick = onBackToLogin) {
                Text("Back to Login", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Create Account", color = stageKeeperPurple, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Join the party securely.", color = Color.LightGray, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(32.dp))

        Text("Required Info", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = username, onValueChange = { username = it }, label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name (e.g. BassHead99)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text("Safety & Festival Details (Optional)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("Emergency Contact Number") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = medicalInfo, onValueChange = { medicalInfo = it }, label = { Text("Medical Info (e.g. Asthma, Allergies)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                val newUser = User(
                    email = email, password = password, username = username, displayName = displayName,
                    phoneNumber = phone.ifBlank { null }, emergencyContact = emergencyContact.ifBlank { null },
                    medicalInfo = medicalInfo.ifBlank { null }, partyCode = ""
                )

                viewModel.registerUser(newUser) { success ->
                    if (success) {
                        Toast.makeText(context, "Account Created! Please Log In.", Toast.LENGTH_LONG).show()
                        onSignUpSuccess()
                    } else {
                        Toast.makeText(context, "Error creating account.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
            shape = RoundedCornerShape(8.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && username.isNotBlank() && displayName.isNotBlank()
        ) {
            Text("Complete Sign Up", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))
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
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)

    var partyExpanded by remember { mutableStateOf(false) }
    val parties = listOf("Create New Crew", "EDC Group", "Rolling Loud Party")

    var festivalExpanded by remember { mutableStateOf(false) }
    val festivals = festivalLocations.keys.toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onLogout) {
                Text("Logout", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text("StageKeeper", color = stageKeeperPurple, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Text("Setup Your Event", color = Color.White, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(48.dp))

        // Dropdown 1: Picking the party/crew
        ExposedDropdownMenuBox(
            expanded = partyExpanded, onExpandedChange = { partyExpanded = !partyExpanded }
        ) {
            OutlinedTextField(
                value = selectedParty, onValueChange = {}, readOnly = true,
                label = { Text("1. Select Party", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = Color(0xFF1A1A1A), unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = partyExpanded, onDismissRequest = { partyExpanded = false }) {
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

        // Dropdown 2: Picking the specific festival
        ExposedDropdownMenuBox(
            expanded = festivalExpanded, onExpandedChange = { festivalExpanded = !festivalExpanded }
        ) {
            OutlinedTextField(
                value = selectedFestival, onValueChange = {}, readOnly = true,
                label = { Text("2. Select Festival", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedContainerColor = Color(0xFF1A1A1A), unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            ExposedDropdownMenu(expanded = festivalExpanded, onDismissRequest = { festivalExpanded = false }) {
                festivals.forEach { festival ->
                    DropdownMenuItem(
                        text = { Text(festival) },
                        onClick = {
                            onFestivalSelected(festival)
                            festivalExpanded = false
                            (context as MainActivity).cacheFestivalMapLocally(festival)
                            Toast.makeText(context, "Caching map for $festival...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { onLaunchMap() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
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
    onNavigateHome: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val locations by viewModel.allLocations.collectAsState()

    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperDark = Color(0xFF050505)

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val redDotBitmap = remember { createSimpleRedDot() }

    var showNoteDialog by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }

    // Dropdown states for Map Screen
    var partyExpanded by remember { mutableStateOf(false) }
    val parties = listOf("Create New Crew", "EDC Group", "Rolling Loud Party")

    var festivalExpanded by remember { mutableStateOf(false) }
    val festivals = festivalLocations.keys.toList()

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
        modifier = Modifier.fillMaxSize().background(stageKeeperDark)
    ) {
        // TOP APP BAR
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
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onNavigateHome) { Text("Home", color = stageKeeperPurple, fontWeight = FontWeight.Bold) }
                Text("StageKeeper", color = stageKeeperPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onLogout) { Text("Logout", color = stageKeeperPurple, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Change Party Dropdown
                ExposedDropdownMenuBox(
                    expanded = partyExpanded, onExpandedChange = { partyExpanded = !partyExpanded }, modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = activeParty, onValueChange = {}, readOnly = true,
                        label = { Text("Party", color = Color.LightGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color(0xFF1A1A1A), unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                    ExposedDropdownMenu(expanded = partyExpanded, onDismissRequest = { partyExpanded = false }) {
                        parties.forEach { party ->
                            DropdownMenuItem(text = { Text(party) }, onClick = { onPartyChange(party); partyExpanded = false })
                        }
                    }
                }

                // Change Festival Dropdown
                ExposedDropdownMenuBox(
                    expanded = festivalExpanded, onExpandedChange = { festivalExpanded = !festivalExpanded }, modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = activeFestival, onValueChange = {}, readOnly = true,
                        label = { Text("Festival", color = Color.LightGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color(0xFF1A1A1A), unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                    ExposedDropdownMenu(expanded = festivalExpanded, onDismissRequest = { festivalExpanded = false }) {
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
                                    FollowPuckViewportStateOptions.Builder().zoom(16.0).build()
                                )
                            )
                        } else {
                            // A specific festival was chosen. Turn location on, but idle the viewport
                            view.location.enabled = true
                            view.viewport.idle()

                            val targetPoint = festivalLocations[activeFestival]

                            // Snap the camera to the specific festival grounds
                            targetPoint?.let { point ->
                                view.mapboxMap.setCamera(
                                    CameraOptions.Builder().center(point).zoom(14.5).build()
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
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { showNoteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Drop Pin", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = { viewModel.deleteAllLocations() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
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