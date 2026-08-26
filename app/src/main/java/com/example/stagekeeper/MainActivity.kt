package com.example.stagekeeper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
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
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.maps.extension.style.sources.generated.imageSource
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mapbox.maps.plugin.attribution.attribution

enum class AppScreen { Splash, Login, SignUp, GoogleSignUp, Setup, Map, Profile, Chat, Lineup, Locked }

/*
// Database of 100 major US music festivals with accurate venue coordinates
// COMMENTED OUT FOR NOW - PRESERVED FOR FUTURE EXPANSION
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
*/

private fun imageQuad(
    topLeftLng: Double, topLeftLat: Double,
    topRightLng: Double, topRightLat: Double,
    bottomRightLng: Double, bottomRightLat: Double,
    bottomLeftLng: Double, bottomLeftLat: Double
): List<List<Double>> {
    return listOf(
        listOf(topLeftLng, topLeftLat),
        listOf(topRightLng, topRightLat),
        listOf(bottomRightLng, bottomRightLat),
        listOf(bottomLeftLng, bottomLeftLat)
    )
}

data class FestivalData(
    val name: String,
    val dates: String,
    val center: Point,
    val defaultZoom: Double = 14.5,
    val imageName: String,
    val imageCoordinates: List<List<Double>>
)

// ACTIVE DATABASE
val upcomingFestivals = listOf(
    FestivalData("Arc Music Festival (IL)", "Sep 4 - Sep 6", Point.fromLngLat(-87.66478, 41.88392), 16.5, "arc_music2026", imageQuad(-87.66795, 41.88562, -87.66302, 41.88562, -87.66302, 41.88136, -87.66795, 41.88136)),
    FestivalData("Riot Fest (IL)", "Sep 18 - Sep 20", Point.fromLngLat(-87.6994, 41.8572), 14.8, "riotfest2025", imageQuad(-87.70340, 41.86125, -87.69175, 41.86125, -87.69175, 41.85530, -87.70340, 41.85530)),
    FestivalData("EDC Orlando (FL)", "Nov 6 - Nov 8", Point.fromLngLat(-81.40275, 28.53902), 15.2, "edcorlando2022", imageQuad(-81.40655, 28.54297, -81.39585, 28.54297, -81.39585, 28.53218, -81.40655, 28.53218)),
    FestivalData("Austin City Limits (TX)", "Oct 2 - Oct 4", Point.fromLngLat(-97.76661, 30.26768), 14.5, "austincitylimits2026", imageQuad(-97.77720, 30.27080, -97.76020, 30.27080, -97.76020, 30.26338, -97.77720, 30.26338)),
    FestivalData("Life is Beautiful (NV)", "Sep 18 - Sep 20", Point.fromLngLat(-115.13656, 36.16931), 15.5, "lifeisbeautiful2023", imageQuad(-115.139147, 36.173818, -115.131481, 36.170475, -115.133510, 36.164371, -115.141628, 36.167792)),
    FestivalData("Lost Lands (OH)", "Sep 25 - Sep 27", Point.fromLngLat(-82.41100, 39.93982), 13.5, "lostlands2025", imageQuad(-82.42915, 39.94535, -82.40470, 39.94420, -82.40555, 39.92870, -82.43000, 39.92985)),
    FestivalData("Burning Man (NV)", "Aug 30 - Sep 7", Point.fromLngLat(-119.207871, 40.783242), 12.5, "burningman2026", imageQuad(-119.206674, 40.815992, -119.164790, 40.784056, -119.209116, 40.750340, -119.251000, 40.782277)),
    FestivalData("Dancefestopia (KS)", "Sep 7 - Sep 13", Point.fromLngLat(-94.668760, 38.400500), 14.5, "dancefestopia2026", imageQuad(-94.675499, 38.404613, -94.663831, 38.404613, -94.663831, 38.396208, -94.675499, 38.396208)),
    FestivalData("Aftershock (CA)", "Oct 8 - Oct 11", Point.fromLngLat(-121.50741, 38.60135), 14.5, "aftershock2026", imageQuad(-121.51058, 38.60470, -121.50073, 38.60470, -121.50073, 38.59800, -121.51058, 38.59800)),
    FestivalData("Electric Zoo (NY)", "Sep 4 - Sep 6", Point.fromLngLat(-73.921154, 40.799337), 15.8, "electriczoo2016", imageQuad(-73.922126, 40.801838, -73.918820, 40.799180, -73.919998, 40.797350, -73.923672, 40.798980)),
    FestivalData("Louder Than Life (KY)", "Sep 24 - Sep 27", Point.fromLngLat(-85.74496, 38.19690), 15.1, "louderthanlife2026", imageQuad(-85.74803, 38.20270, -85.73947, 38.20166, -85.74189, 38.19110, -85.75045, 38.19214))
)


// Verified 2026 festival lineup/schedule dataset
// Verified against current sources on August 26, 2026.
// Blank fields are INTENTIONAL: they mean the requested detail was not verifiably published in a source I was willing to trust.
// Genre uses artist-level best-fit tags for practical app filtering. Crossover acts may have multiple genres; obscure/local acts use conservative festival-appropriate categories.

data class FestivalArtist(
    val artistName: String,
    val stage: String,
    val day: String,
    val startTime: String = "",
    val endTime: String = "",
    val genre: String = ""
)

// ============================================================
// ARC Music Festival (IL) — 96 verified entries
// ARC schedule is cross-checked against multiple current 2026 schedule mirrors because the festival’s indexed set-times page was still serving stale 2025 text at verification time.
// ============================================================
val arcMusicFestival2026 = listOf(
    FestivalArtist(
        artistName = "Virago",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "Techno / House"
    ),
    FestivalArtist(
        artistName = "INVT",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Latin Club / Bass / Breaks"
    ),
    FestivalArtist(
        artistName = "Azzecca",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "KI/KI",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Hard Techno / Trance"
    ),
    FestivalArtist(
        artistName = "Chase & Status",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Sara Landry Presents Eternalism",
        stage = "THE GRID",
        day = "Friday, September 4",
        startTime = "8:45 PM",
        endTime = "10:00 PM",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Muffy",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "IDEMI",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Tiga B2B Brunello",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Electro / Techno"
    ),
    FestivalArtist(
        artistName = "Max Dean B2B Luke Dean",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Prospa",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "UK Bass / Garage / Breaks"
    ),
    FestivalArtist(
        artistName = "Chris Stussy",
        stage = "EXPANSIONS",
        day = "Friday, September 4",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Frechette",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Canary Yellow",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "camoufly",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "4:00 PM",
        endTime = "5:00 PM",
        genre = "Future Bass / UK Garage"
    ),
    FestivalArtist(
        artistName = "ZULAN",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "5:00 PM",
        endTime = "6:00 PM",
        genre = "Club / Techno"
    ),
    FestivalArtist(
        artistName = "Silva Bumpa",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "6:00 PM",
        endTime = "7:00 PM",
        genre = "UK Garage / Bassline"
    ),
    FestivalArtist(
        artistName = "Pegassi",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "7:00 PM",
        endTime = "8:00 PM",
        genre = "Trance / Eurodance / Hard House"
    ),
    FestivalArtist(
        artistName = "Bad Boombox B2B Mischluft",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "8:00 PM",
        endTime = "9:00 PM",
        genre = "Hardgroove / Techno"
    ),
    FestivalArtist(
        artistName = "Joy Orbison B2B Ben UFO",
        stage = "AREA 909",
        day = "Friday, September 4",
        startTime = "9:00 PM",
        endTime = "10:00 PM",
        genre = "UK Bass / Garage / Breaks"
    ),
    FestivalArtist(
        artistName = "Dabura B2B Anna Maria",
        stage = "THE MIDWAY",
        day = "Friday, September 4",
        startTime = "2:00 PM",
        endTime = "4:00 PM",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "Notre Dame",
        stage = "THE MIDWAY",
        day = "Friday, September 4",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Korolova B2B Kasia",
        stage = "THE MIDWAY",
        day = "Friday, September 4",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Eli & Fur B2B Cristoph",
        stage = "THE MIDWAY",
        day = "Friday, September 4",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Meduza B2B Genesi",
        stage = "THE MIDWAY",
        day = "Friday, September 4",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "M3RCH",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Très Mortimer B2B Cole Knight",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Omar+ B2B Obskür",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Odd Mob",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "Cloonee",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "7:00 PM",
        endTime = "8:25 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "MAU P",
        stage = "THE GRID",
        day = "Saturday, September 5",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Flores Negras",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "DJ Hyperactive B2B Lindsey Herbert",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "Dax J Live",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "4:00 PM",
        endTime = "5:00 PM",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "999999999",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "5:00 PM",
        endTime = "6:30 PM",
        genre = "Acid Techno / Hard Techno"
    ),
    FestivalArtist(
        artistName = "I Hate Models",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "6:30 PM",
        endTime = "8:30 PM",
        genre = "Hard Techno / Trance"
    ),
    FestivalArtist(
        artistName = "Nico Moreno",
        stage = "EXPANSIONS",
        day = "Saturday, September 5",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Superjane (DJ Heather, Colette, DJ Lady D, Dayhota)",
        stage = "AREA 909",
        day = "Saturday, September 5",
        startTime = "2:00 PM",
        endTime = "4:00 PM",
        genre = "Chicago House"
    ),
    FestivalArtist(
        artistName = "CHAOS IN THE CBD",
        stage = "AREA 909",
        day = "Saturday, September 5",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Deep House / House"
    ),
    FestivalArtist(
        artistName = "Detroit Love (Carl Craig, Moodymann, Stacey Pullen)",
        stage = "AREA 909",
        day = "Saturday, September 5",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Detroit Techno / House"
    ),
    FestivalArtist(
        artistName = "Salute",
        stage = "AREA 909",
        day = "Saturday, September 5",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "The Blessed Madonna B2B Lil’ Louis",
        stage = "AREA 909",
        day = "Saturday, September 5",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Chicago House"
    ),
    FestivalArtist(
        artistName = "JJ Illgen B2B Janesita",
        stage = "THE MIDWAY",
        day = "Saturday, September 5",
        startTime = "2:00 PM",
        endTime = "3:30 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Laurence Guy B2B Monty Kiddo",
        stage = "THE MIDWAY",
        day = "Saturday, September 5",
        startTime = "3:30 PM",
        endTime = "5:00 PM",
        genre = "Deep House / House"
    ),
    FestivalArtist(
        artistName = "Discip B2B Roddy Lima",
        stage = "THE MIDWAY",
        day = "Saturday, September 5",
        startTime = "5:00 PM",
        endTime = "6:30 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Luuk van Dijk B2B Sidney Charles",
        stage = "THE MIDWAY",
        day = "Saturday, September 5",
        startTime = "6:30 PM",
        endTime = "8:00 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Nicole Moudaber B2B Paco Osuna B2B Dubfire",
        stage = "THE MIDWAY",
        day = "Saturday, September 5",
        startTime = "8:00 PM",
        endTime = "10:00 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Banchan",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Son of Son",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Melodic Techno"
    ),
    FestivalArtist(
        artistName = "Swimming Paul",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "4:00 PM",
        endTime = "5:20 PM",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "Chris Avantgarde B2B Kevin de Vries",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "5:20 PM",
        endTime = "6:50 PM",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Underworld",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "7:15 PM",
        endTime = "8:15 PM",
        genre = "Techno / Progressive House"
    ),
    FestivalArtist(
        artistName = "Anyma",
        stage = "THE GRID",
        day = "Sunday, September 6",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Nick C",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Locklead",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Jamback B2B Marsolo",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Dennis Cruz B2B Ben Sterling",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Beltran",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Mochakk",
        stage = "EXPANSIONS",
        day = "Sunday, September 6",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "10cust",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "2:00 PM",
        endTime = "3:30 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Pluko DJ Set",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "3:30 PM",
        endTime = "4:30 PM",
        genre = "Future Bass / Electronic"
    ),
    FestivalArtist(
        artistName = "MCR-T",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "4:30 PM",
        endTime = "5:30 PM",
        genre = "Ghettotech / Electro / Techno"
    ),
    FestivalArtist(
        artistName = "Nia Archives B2B X CLUB.",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Jungle / Drum & Bass"
    ),
    FestivalArtist(
        artistName = "DJ Gigola B2B Skin on Skin",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "7:00 PM",
        endTime = "8:45 PM",
        genre = "Techno / Hard House"
    ),
    FestivalArtist(
        artistName = "Brutalismus 3000",
        stage = "AREA 909",
        day = "Sunday, September 6",
        startTime = "9:00 PM",
        endTime = "10:00 PM",
        genre = "Hard Techno / Electro-punk"
    ),
    FestivalArtist(
        artistName = "Rika B",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "Techno / House"
    ),
    FestivalArtist(
        artistName = "Mike Dunn",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Chicago House"
    ),
    FestivalArtist(
        artistName = "Will Clarke",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Biscits",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Walker & Royce B2B VNSSA",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "After Midnight (Matroda x San Pacho)",
        stage = "THE MIDWAY",
        day = "Sunday, September 6",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Madeline",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Silvie Loto B2B Kinahau",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "3:00 PM",
        endTime = "4:15 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Derrick Carter",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "4:15 PM",
        endTime = "5:30 PM",
        genre = "Chicago House"
    ),
    FestivalArtist(
        artistName = "Honey Dijon",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Michael Bibi",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Green Velvet B2B Josh Baker",
        stage = "THE GRID",
        day = "Monday, September 7",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Oogie B2B Phives",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "LuSiD",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "ANNA B2B Traumer",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Josh Baker",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Carlita B2B WhoMadeWho",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Indie Dance / House"
    ),
    FestivalArtist(
        artistName = "Vintage Culture B2B Damian Lazarus",
        stage = "EXPANSIONS",
        day = "Monday, September 7",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Indie Dance / House"
    ),
    FestivalArtist(
        artistName = "Kirk",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Hotpretty",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Bushbaby",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "4:00 PM",
        endTime = "5:00 PM",
        genre = "UK Garage / Bassline"
    ),
    FestivalArtist(
        artistName = "Sam Alfred B2B Club Angel",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "5:00 PM",
        endTime = "6:00 PM",
        genre = "Hardgroove / Techno"
    ),
    FestivalArtist(
        artistName = "Bullet Tooth",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "6:00 PM",
        endTime = "7:00 PM",
        genre = "UK Garage / Bass"
    ),
    FestivalArtist(
        artistName = "DJ Heartstring B2B Baugruppe90",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Trance / Eurodance / Hard House"
    ),
    FestivalArtist(
        artistName = "Boys Noize B2B Hiroko Yamamura",
        stage = "AREA 909",
        day = "Monday, September 7",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Electro / Techno"
    ),
    FestivalArtist(
        artistName = "r00bies4ever",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "2:00 PM",
        endTime = "3:00 PM",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Dunes of Dawn",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "3:00 PM",
        endTime = "4:00 PM",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "OGUZ",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "4:00 PM",
        endTime = "5:30 PM",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Quest B2B Ellen Allien",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "5:30 PM",
        endTime = "7:00 PM",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "Adrian Mills B2B Fumi B2B Serafina",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "7:00 PM",
        endTime = "8:30 PM",
        genre = "Hardgroove / Techno"
    ),
    FestivalArtist(
        artistName = "KLOUD",
        stage = "THE MIDWAY",
        day = "Monday, September 7",
        startTime = "8:30 PM",
        endTime = "10:00 PM",
        genre = "Electro / Techno"
    ),
)

// ============================================================
// Riot Fest (IL) — 106 verified entries
// Official Riot Fest 2026 schedule; end times are included because the official schedule grid publishes them.
// ============================================================
val riotFest2026 = listOf(
    FestivalArtist(
        artistName = "Cardinals",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "12:00 PM",
        endTime = "12:30 PM",
        genre = "Indie Rock / Post-Punk"
    ),
    FestivalArtist(
        artistName = "JMSN",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "1:10 PM",
        endTime = "1:40 PM",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "Violet Grohl",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "2:20 PM",
        endTime = "2:50 PM",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "The Paradox",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "3:30 PM",
        endTime = "4:00 PM",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Joey Valence & Brae",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "4:40 PM",
        endTime = "5:20 PM",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "The All-American Rejects",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "6:30 PM",
        endTime = "7:30 PM",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Twenty One Pilots",
        stage = "RIOT",
        day = "Friday, September 18",
        startTime = "8:40 PM",
        endTime = "10:00 PM",
        genre = "Alternative Rock / Hip-Hop / Pop"
    ),
    FestivalArtist(
        artistName = "Teen Mortgage",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "12:35 PM",
        endTime = "1:05 PM",
        genre = "Garage Punk / Stoner Rock"
    ),
    FestivalArtist(
        artistName = "Fleshwater",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "1:45 PM",
        endTime = "2:15 PM",
        genre = "Shoegaze / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "3OH!3",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "2:55 PM",
        endTime = "3:25 PM",
        genre = "Electropop / Crunkcore"
    ),
    FestivalArtist(
        artistName = "Bayside",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "4:05 PM",
        endTime = "4:35 PM",
        genre = "Punk Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Alkaline Trio",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "5:25 PM",
        endTime = "6:25 PM",
        genre = "Punk Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Rise Against",
        stage = "ROOTS",
        day = "Friday, September 18",
        startTime = "7:35 PM",
        endTime = "8:35 PM",
        genre = "Punk Rock / Melodic Hardcore"
    ),
    FestivalArtist(
        artistName = "Glixen",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "12:15 PM",
        endTime = "12:45 PM",
        genre = "Shoegaze"
    ),
    FestivalArtist(
        artistName = "División Minúscula",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "1:15 PM",
        endTime = "1:45 PM",
        genre = "Alternative Rock / Punk Rock"
    ),
    FestivalArtist(
        artistName = "Radio Free Alice",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "2:15 PM",
        endTime = "2:45 PM",
        genre = "Post-Punk / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Tricky",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "3:15 PM",
        endTime = "4:00 PM",
        genre = "Trip-Hop"
    ),
    FestivalArtist(
        artistName = "Motion City Soundtrack",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "4:45 PM",
        endTime = "5:25 PM",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Santigold",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "6:15 PM",
        endTime = "7:15 PM",
        genre = "Art Pop / New Wave / Electronic"
    ),
    FestivalArtist(
        artistName = "Pixies",
        stage = "REBEL",
        day = "Friday, September 18",
        startTime = "8:15 PM",
        endTime = "9:15 PM",
        genre = "Alternative Rock / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Panic Shack",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "12:30 PM",
        endTime = "1:00 PM",
        genre = "Post-Punk"
    ),
    FestivalArtist(
        artistName = "Soul Glo",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "1:30 PM",
        endTime = "2:00 PM",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Mariachi El Bronx",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "2:30 PM",
        endTime = "3:00 PM",
        genre = "Mariachi / Rock"
    ),
    FestivalArtist(
        artistName = "Bratmobile",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "3:30 PM",
        endTime = "4:00 PM",
        genre = "Riot Grrrl / Punk"
    ),
    FestivalArtist(
        artistName = "Guttermouth",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "4:30 PM",
        endTime = "5:00 PM",
        genre = "Skate Punk"
    ),
    FestivalArtist(
        artistName = "Slick Rick",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "5:30 PM",
        endTime = "6:15 PM",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Sex Pistols feat. Frank Carter",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "7:00 PM",
        endTime = "8:00 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "Iggy Pop",
        stage = "RISE",
        day = "Friday, September 18",
        startTime = "8:45 PM",
        endTime = "9:45 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "Almost There But Not Really",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "12:15 PM",
        endTime = "12:45 PM",
        genre = "Alternative Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Greet Death",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "1:15 PM",
        endTime = "1:45 PM",
        genre = "Shoegaze / Slowcore / Emo"
    ),
    FestivalArtist(
        artistName = "Slothrust",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "2:15 PM",
        endTime = "2:45 PM",
        genre = "Alternative Rock / Blues Rock"
    ),
    FestivalArtist(
        artistName = "Worry Club",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "3:15 PM",
        endTime = "3:45 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Stephen Egerton",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "4:15 PM",
        endTime = "4:45 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "The Callous Daoboys",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "5:15 PM",
        endTime = "5:45 PM",
        genre = "Experimental Metal / Mathcore"
    ),
    FestivalArtist(
        artistName = "DeathbyRomy",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "6:15 PM",
        endTime = "6:45 PM",
        genre = "Alternative Pop / Industrial Rock"
    ),
    FestivalArtist(
        artistName = "Foxy Shazam",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "7:15 PM",
        endTime = "7:45 PM",
        genre = "Glam Rock / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "GWAR",
        stage = "RADICAL",
        day = "Friday, September 18",
        startTime = "8:55 PM",
        endTime = "9:55 PM",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "ASAVA",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "12:30 PM",
        endTime = "1:00 PM",
        genre = "Heavy Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Melt-Banana",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "1:35 PM",
        endTime = "2:05 PM",
        genre = "Noise Rock / Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Angine de Poitrine",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "2:45 PM",
        endTime = "3:15 PM",
        genre = "Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Public Image Ltd",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "4:05 PM",
        endTime = "4:45 PM",
        genre = "Post-Punk"
    ),
    FestivalArtist(
        artistName = "Social Distortion",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "5:55 PM",
        endTime = "6:55 PM",
        genre = "Cowpunk / Punk Rock"
    ),
    FestivalArtist(
        artistName = "Tool",
        stage = "RIOT",
        day = "Saturday, September 19",
        startTime = "8:20 PM",
        endTime = "10:00 PM",
        genre = "Progressive Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Kiwi Jr.",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "12:00 PM",
        endTime = "12:30 PM",
        genre = "Alternative Rock / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Yard Act",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "1:00 PM",
        endTime = "1:30 PM",
        genre = "Post-Punk"
    ),
    FestivalArtist(
        artistName = "Brian Fallon",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "2:10 PM",
        endTime = "2:40 PM",
        genre = "Heartland Rock / Folk Rock"
    ),
    FestivalArtist(
        artistName = "Bright Eyes",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "3:20 PM",
        endTime = "4:00 PM",
        genre = "Indie Rock / Folk"
    ),
    FestivalArtist(
        artistName = "Sugar",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "4:50 PM",
        endTime = "5:50 PM",
        genre = "Alternative Rock / Power Pop"
    ),
    FestivalArtist(
        artistName = "Morrissey",
        stage = "ROOTS",
        day = "Saturday, September 19",
        startTime = "7:00 PM",
        endTime = "8:15 PM",
        genre = "Alternative Rock / Indie Pop"
    ),
    FestivalArtist(
        artistName = "NOBRO",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "12:05 PM",
        endTime = "12:35 PM",
        genre = "Garage Punk / Rock"
    ),
    FestivalArtist(
        artistName = "Strike Anywhere",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "1:00 PM",
        endTime = "1:30 PM",
        genre = "Punk Rock / Melodic Hardcore"
    ),
    FestivalArtist(
        artistName = "The Chats",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "1:55 PM",
        endTime = "2:25 PM",
        genre = "Garage Punk / Rock"
    ),
    FestivalArtist(
        artistName = "Destroy Boys",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "2:55 PM",
        endTime = "3:25 PM",
        genre = "Garage Punk / Rock"
    ),
    FestivalArtist(
        artistName = "Thrice",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "3:50 PM",
        endTime = "4:30 PM",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Descendents",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "4:55 PM",
        endTime = "5:55 PM",
        genre = "Punk Rock / Pop Punk"
    ),
    FestivalArtist(
        artistName = "Bad Religion",
        stage = "REBEL",
        day = "Saturday, September 19",
        startTime = "7:20 PM",
        endTime = "8:20 PM",
        genre = "Skate Punk"
    ),
    FestivalArtist(
        artistName = "Whispers",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "12:00 PM",
        endTime = "12:30 PM",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Show Me the Body",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "1:00 PM",
        endTime = "1:30 PM",
        genre = "Noise Rock / Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "The Suicide Machines",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "2:00 PM",
        endTime = "2:50 PM",
        genre = "Ska Punk"
    ),
    FestivalArtist(
        artistName = "Less Than Jake",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "3:20 PM",
        endTime = "4:00 PM",
        genre = "Ska Punk"
    ),
    FestivalArtist(
        artistName = "PUP",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "4:30 PM",
        endTime = "5:15 PM",
        genre = "Punk Rock / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Gogol Bordello",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "6:30 PM",
        endTime = "7:30 PM",
        genre = "Gypsy Punk"
    ),
    FestivalArtist(
        artistName = "Nas",
        stage = "RISE",
        day = "Saturday, September 19",
        startTime = "9:00 PM",
        endTime = "10:00 PM",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Aim High",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "12:00 PM",
        endTime = "12:30 PM",
        genre = "Pop Punk / Easycore"
    ),
    FestivalArtist(
        artistName = "The Iron Roses",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "12:45 PM",
        endTime = "1:15 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "Burning Airlines",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "1:45 PM",
        endTime = "2:15 PM",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Hot Rod Circuit",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "2:45 PM",
        endTime = "3:15 PM",
        genre = "Emo / Pop Punk / Alternative"
    ),
    FestivalArtist(
        artistName = "Gurriers",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "3:45 PM",
        endTime = "4:15 PM",
        genre = "Post-Punk"
    ),
    FestivalArtist(
        artistName = "Frankie & The Witch Fingers",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "4:45 PM",
        endTime = "5:15 PM",
        genre = "Psychedelic / Garage Rock"
    ),
    FestivalArtist(
        artistName = "VANA",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "5:45 PM",
        endTime = "6:15 PM",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Chat Pile",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "6:45 PM",
        endTime = "7:30 PM",
        genre = "Sludge Metal / Noise Rock"
    ),
    FestivalArtist(
        artistName = "Afroman",
        stage = "RADICAL",
        day = "Saturday, September 19",
        startTime = "8:00 PM",
        endTime = "9:00 PM",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "STOMACH BOOK",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "12:40 PM",
        endTime = "1:10 PM",
        genre = "Experimental Rock / Digital Hardcore"
    ),
    FestivalArtist(
        artistName = "Holding Absence",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "1:50 PM",
        endTime = "2:20 PM",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Arm's Length",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "3:00 PM",
        endTime = "3:30 PM",
        genre = "Emo / Pop Punk"
    ),
    FestivalArtist(
        artistName = "Pennywise",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "4:25 PM",
        endTime = "5:05 PM",
        genre = "Skate Punk"
    ),
    FestivalArtist(
        artistName = "Taking Back Sunday",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "6:15 PM",
        endTime = "7:15 PM",
        genre = "Emo / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Pierce The Veil",
        stage = "RIOT",
        day = "Sunday, September 20",
        startTime = "8:25 PM",
        endTime = "9:55 PM",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Remember Sports",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "12:05 PM",
        endTime = "12:35 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Daisy Grenade",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "1:15 PM",
        endTime = "1:45 PM",
        genre = "Emo / Pop Punk"
    ),
    FestivalArtist(
        artistName = "Sincere Engineer",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "2:25 PM",
        endTime = "2:55 PM",
        genre = "Punk Rock / Emo"
    ),
    FestivalArtist(
        artistName = "The Beths",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "3:35 PM",
        endTime = "4:20 PM",
        genre = "Indie Rock / Power Pop"
    ),
    FestivalArtist(
        artistName = "The Format",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "5:10 PM",
        endTime = "6:10 PM",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Alanis Morissette",
        stage = "ROOTS",
        day = "Sunday, September 20",
        startTime = "7:20 PM",
        endTime = "8:20 PM",
        genre = "Alternative Rock / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Largemouth",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "12:00 PM",
        endTime = "12:30 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Algernon Cadwallader",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "1:05 PM",
        endTime = "1:35 PM",
        genre = "Emo / Math Rock"
    ),
    FestivalArtist(
        artistName = "Pretty Girls Make Graves",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "2:05 PM",
        endTime = "2:35 PM",
        genre = "Post-Punk / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Jejune",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "3:05 PM",
        endTime = "3:45 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "This Is Lorelei",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "4:15 PM",
        endTime = "4:55 PM",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "William Shatner",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "5:25 PM",
        endTime = "6:00 PM",
        genre = "Spoken Word / Rock"
    ),
    FestivalArtist(
        artistName = "Twin Peaks",
        stage = "REBEL",
        day = "Sunday, September 20",
        startTime = "6:30 PM",
        endTime = "7:30 PM",
        genre = "Indie Rock / Garage Rock"
    ),
    FestivalArtist(
        artistName = "Macseal",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "12:50 PM",
        endTime = "1:20 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Ben Quad",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "1:50 PM",
        endTime = "2:20 PM",
        genre = "Emo / Math Rock"
    ),
    FestivalArtist(
        artistName = "Saturdays At Your Place",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "2:50 PM",
        endTime = "3:20 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Cartel",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "3:50 PM",
        endTime = "4:30 PM",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Mom Jeans",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "5:00 PM",
        endTime = "5:40 PM",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Patti Smith and Her Band",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "6:10 PM",
        endTime = "7:10 PM",
        genre = "Art Rock / Punk"
    ),
    FestivalArtist(
        artistName = "Elvis Costello & The Imposters",
        stage = "RISE",
        day = "Sunday, September 20",
        startTime = "8:30 PM",
        endTime = "9:30 PM",
        genre = "New Wave / Power Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Rose of Demise",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "12:35 PM",
        endTime = "1:05 PM",
        genre = "Hardcore / Punk"
    ),
    FestivalArtist(
        artistName = "Murphy's Law",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "1:35 PM",
        endTime = "2:15 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "Haywire",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "2:45 PM",
        endTime = "3:25 PM",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Dead To Me",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "3:55 PM",
        endTime = "4:35 PM",
        genre = "Punk Rock"
    ),
    FestivalArtist(
        artistName = "The Flatliners",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "5:05 PM",
        endTime = "5:45 PM",
        genre = "Skate Punk"
    ),
    FestivalArtist(
        artistName = "Good Riddance",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "6:15 PM",
        endTime = "6:55 PM",
        genre = "Skate Punk"
    ),
    FestivalArtist(
        artistName = "Bowling For Soup",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "7:25 PM",
        endTime = "8:05 PM",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Insane Clown Posse",
        stage = "RADICAL",
        day = "Sunday, September 20",
        startTime = "8:55 PM",
        endTime = "9:55 PM",
        genre = "Horrorcore Hip-Hop"
    ),
)

// ============================================================
// EDC Orlando (FL) — 108 verified entries
// Official 2026 lineup by day. EDC Orlando says set times are posted in the days leading up to the festival; no official 2026 stage/time grid was available as of Aug. 26.
// ============================================================
val edcOrlando2026 = listOf(
    FestivalArtist(
        artistName = "AAT",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Adventure Club (Sunset Set)",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Afrojack",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Alesso (Sunset Set)",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / EDM"
    ),
    FestivalArtist(
        artistName = "Azzecca",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Benda B2B Vastive",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Big Florida",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Bou B2B Kanine",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Brunello (Sunset Set)",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Bullet Tooth B2B Sidney Charles",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Chris Lorenzo",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "David Guetta",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / EDM"
    ),
    FestivalArtist(
        artistName = "HAYLA",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / House"
    ),
    FestivalArtist(
        artistName = "IDEMI",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Inbal",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Interplanetary Criminal",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "UK Bass / Garage / Breaks"
    ),
    FestivalArtist(
        artistName = "JOA",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Josh Baker",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Minimal / Deep Tech"
    ),
    FestivalArtist(
        artistName = "Joshwa",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Kompany",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "KREAM",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Progressive House / Trance"
    ),
    FestivalArtist(
        artistName = "Level Up",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Levity",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "MALUGI (Sunset Set)",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Trance / Eurodance / Hard House"
    ),
    FestivalArtist(
        artistName = "Matthias",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Mau P",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "MPH",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "UK Bass / Garage / Breaks"
    ),
    FestivalArtist(
        artistName = "Omar+",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Pegassi",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Trance / Eurodance / Hard House"
    ),
    FestivalArtist(
        artistName = "Prospa B2B Josh Baker",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Prospa",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "UK Bass / Garage / Breaks"
    ),
    FestivalArtist(
        artistName = "RAJE",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Sloth",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Whethan",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Future Bass / Electronic"
    ),
    FestivalArtist(
        artistName = "Wooli",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Zack Martino",
        stage = "",
        day = "Friday, November 6",
        startTime = "",
        endTime = "",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "Aaron Hibell",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Progressive House / Trance"
    ),
    FestivalArtist(
        artistName = "ACRAZE B2B CID",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Bass House"
    ),
    FestivalArtist(
        artistName = "Alan Walker (Sunset Set)",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / EDM"
    ),
    FestivalArtist(
        artistName = "Alison Wonderland",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Future Bass / Trap"
    ),
    FestivalArtist(
        artistName = "ALLEYCVT",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Alves",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "AVELLO",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "AYYBO",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "ChaseWest",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Dennis Cruz",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Devault (Sunset Set)",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Indie Dance / Electronic"
    ),
    FestivalArtist(
        artistName = "Discip",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Disco Lines",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "Fallon",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Franky Rizardo",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Fury with MC Dino",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Gabss",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Greg 99",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Jkyl & Hyde",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Kaskade",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "KinAhau",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "LAYZ",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "MADVKTM",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Mai Iachetti",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Max Dean, Luke Dean",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Me n ü",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Miguelle & Tons",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Monoky",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Techno"
    ),
    FestivalArtist(
        artistName = "Nico Moreno",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Ray Volpe",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Roddy Lima",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Tech House"
    ),
    FestivalArtist(
        artistName = "Rossi. (Sunset Set)",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Skull Machine (Black Tiger Sex Machine x Kai Wachi)",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Steve Aoki",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Subsonic",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Twinsick",
        stage = "",
        day = "Saturday, November 7",
        startTime = "",
        endTime = "",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "A Little Sound",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Jungle / Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Adrián Mills",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Alok",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / EDM"
    ),
    FestivalArtist(
        artistName = "AR/CO",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / Electronic"
    ),
    FestivalArtist(
        artistName = "ATLiens",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Boogie T",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Boys Noize B2B Brutalismus 3000",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno / Electro-punk"
    ),
    FestivalArtist(
        artistName = "Chef Boyarbeatz",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "CØNTRA",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Deorro B2B DJ Diesel",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electro House / Bass Music"
    ),
    FestivalArtist(
        artistName = "Discovery Project",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "ESSE",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Hardwell",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Holy Priest",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "I Hate Models",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno / Trance"
    ),
    FestivalArtist(
        artistName = "Ian Asher",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dance-pop / Electronic"
    ),
    FestivalArtist(
        artistName = "Jessica Audiffred",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Kaivon",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Future Bass / Electronic"
    ),
    FestivalArtist(
        artistName = "KI/KI",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno / Trance"
    ),
    FestivalArtist(
        artistName = "Klangkuenstler",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Hard Techno"
    ),
    FestivalArtist(
        artistName = "Know Good",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "M81!",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
    FestivalArtist(
        artistName = "Maddix",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Marlon Hoffstadt (Sunset Set)",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Trance / Eurodance / Hard House"
    ),
    FestivalArtist(
        artistName = "Martin Garrix",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Meduza",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Melodic House / Techno"
    ),
    FestivalArtist(
        artistName = "Of The Trees (Sunset Set)",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "phrva",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ravenscoon",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "San Holo (Wholesome Riddim Set)",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Melodic Bass / Future Bass"
    ),
    FestivalArtist(
        artistName = "SHDW",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Techno"
    ),
    FestivalArtist(
        artistName = "Sippy",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SLANDER (Sunset Set)",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "Taiki Nulight",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "TroyBoi",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Ultrathem",
        stage = "",
        day = "Sunday, November 8",
        startTime = "",
        endTime = "",
        genre = "Electronic Dance Music"
    ),
)

// ============================================================
// Austin City Limits (TX) — 190 verified entries
// ACL has published its 2026 schedule. Start times and stages are populated; end times are left blank because the available schedule transcription publishes starts rather than exact end times.
// ============================================================
val austinCityLimits2026 = listOf(
    FestivalArtist(
        artistName = "The 4411",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Asleep at the Wheel",
        stage = "T-Mobile",
        day = "Weekend 1 — Friday, October 2",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Western Swing / Country"
    ),
    FestivalArtist(
        artistName = "Hunx and His Punx",
        stage = "American Express",
        day = "Weekend 1 — Friday, October 2",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Garage Punk / Rock"
    ),
    FestivalArtist(
        artistName = "Faouzia",
        stage = "Miller Lite",
        day = "Weekend 1 — Friday, October 2",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Elle Coves",
        stage = "BMI",
        day = "Weekend 1 — Friday, October 2",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Night Traveler",
        stage = "BeatBox",
        day = "Weekend 1 — Friday, October 2",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Synthpop / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Solomon Hicks",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Blues / Roots Rock"
    ),
    FestivalArtist(
        artistName = "Elijah Delgado",
        stage = "Snapchat",
        day = "Weekend 1 — Friday, October 2",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie / Alternative / Pop"
    ),
    FestivalArtist(
        artistName = "New Constellations",
        stage = "T-Mobile",
        day = "Weekend 1 — Friday, October 2",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Indie Pop / Dream Pop"
    ),
    FestivalArtist(
        artistName = "CMAT",
        stage = "American Express",
        day = "Weekend 1 — Friday, October 2",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Country Pop / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Paris Paloma",
        stage = "Miller Lite",
        day = "Weekend 1 — Friday, October 2",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Bo Staloch",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Izzy Escobar",
        stage = "BMI",
        day = "Weekend 1 — Friday, October 2",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie / Alternative / Pop"
    ),
    FestivalArtist(
        artistName = "Marlon Funaki",
        stage = "BeatBox",
        day = "Weekend 1 — Friday, October 2",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Rock / Psychedelic Rock"
    ),
    FestivalArtist(
        artistName = "LP",
        stage = "Snapchat",
        day = "Weekend 1 — Friday, October 2",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Jesse Welles",
        stage = "T-Mobile",
        day = "Weekend 1 — Friday, October 2",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Folk Rock / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Rebecca Black",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Hyperpop / Electropop"
    ),
    FestivalArtist(
        artistName = "Amyl and the Sniffers",
        stage = "American Express",
        day = "Weekend 1 — Friday, October 2",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Punk Rock / Garage Punk"
    ),
    FestivalArtist(
        artistName = "Brandon Flowers",
        stage = "Miller Lite",
        day = "Weekend 1 — Friday, October 2",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Alternative Rock / Synthpop"
    ),
    FestivalArtist(
        artistName = "Grocery Bag",
        stage = "BMI",
        day = "Weekend 1 — Friday, October 2",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Pilsowsky",
        stage = "BeatBox",
        day = "Weekend 1 — Friday, October 2",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "BUNT.",
        stage = "Snapchat",
        day = "Weekend 1 — Friday, October 2",
        startTime = "5:30 PM",
        endTime = "",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "Turnstile",
        stage = "T-Mobile",
        day = "Weekend 1 — Friday, October 2",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Hardcore Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Steve Aoki",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Labrinth",
        stage = "American Express",
        day = "Weekend 1 — Friday, October 2",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Alternative R&B / Electronic"
    ),
    FestivalArtist(
        artistName = "Leon Thomas",
        stage = "Miller Lite",
        day = "Weekend 1 — Friday, October 2",
        startTime = "7:15 PM",
        endTime = "",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "Molly Santana",
        stage = "BeatBox",
        day = "Weekend 1 — Friday, October 2",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "The Chainsmokers",
        stage = "Snapchat",
        day = "Weekend 1 — Friday, October 2",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Electropop / Dance"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Friday, October 2",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "Skrillex",
        stage = "T-Mobile",
        day = "Weekend 1 — Friday, October 2",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Charli xcx",
        stage = "American Express",
        day = "Weekend 1 — Friday, October 2",
        startTime = "8:40 PM",
        endTime = "",
        genre = "Hyperpop / Electropop"
    ),
    FestivalArtist(
        artistName = "Fightmaster",
        stage = "BMI",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Left Lucid",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Night Tapes",
        stage = "T-Mobile",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Synthpop / Dream Pop"
    ),
    FestivalArtist(
        artistName = "Annie DiRusso",
        stage = "American Express",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Indie Rock / Pop"
    ),
    FestivalArtist(
        artistName = "Temper City",
        stage = "Miller Lite",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Emma Ogier",
        stage = "BMI",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Cure for Paranoia",
        stage = "BeatBox",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Alternative Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "DJ Cassandra",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "2:00 PM",
        endTime = "",
        genre = "House / Electronic"
    ),
    FestivalArtist(
        artistName = "Rochelle Jordan",
        stage = "Snapchat",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Alternative R&B / Electronic"
    ),
    FestivalArtist(
        artistName = "Balu Brigada",
        stage = "T-Mobile",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Finn Wolfhard",
        stage = "American Express",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Arcy Drive",
        stage = "Miller Lite",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Indie Rock / Garage Rock"
    ),
    FestivalArtist(
        artistName = "Don West",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Soul / R&B"
    ),
    FestivalArtist(
        artistName = "Coleman Jennings",
        stage = "BMI",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Ryan Beatty",
        stage = "BeatBox",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Alternative R&B / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Skye Newman",
        stage = "Snapchat",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Pop / R&B"
    ),
    FestivalArtist(
        artistName = "Suki Waterhouse",
        stage = "T-Mobile",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Rodrigo y Gabriela",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Acoustic / Flamenco Rock"
    ),
    FestivalArtist(
        artistName = "Young Miko",
        stage = "American Express",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Latin Trap / Reggaeton"
    ),
    FestivalArtist(
        artistName = "Snow Strippers",
        stage = "Miller Lite",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Electroclash / Dance-pop"
    ),
    FestivalArtist(
        artistName = "Fai Laci",
        stage = "BMI",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Palace",
        stage = "BeatBox",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "It’s Murph",
        stage = "Snapchat",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "5:30 PM",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Bleachers",
        stage = "T-Mobile",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "¥ØU\$UK€ ¥UK1MAT\$U",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Experimental Electronic / Techno"
    ),
    FestivalArtist(
        artistName = "Lola Young",
        stage = "American Express",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Alternative Pop / Soul"
    ),
    FestivalArtist(
        artistName = "Levity",
        stage = "Miller Lite",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "7:15 PM",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "fakemink",
        stage = "BeatBox",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Hip-Hop / Experimental Rap"
    ),
    FestivalArtist(
        artistName = "Lykke Li",
        stage = "Snapchat",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Indie Pop / Synthpop"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "Lorde",
        stage = "T-Mobile",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Art Pop / Electropop"
    ),
    FestivalArtist(
        artistName = "RÜFÜS DU SOL",
        stage = "American Express",
        day = "Weekend 1 — Saturday, October 3",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Indie Electronic / House"
    ),
    FestivalArtist(
        artistName = "Rubio",
        stage = "BMI",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Alternative Pop / Electronic"
    ),
    FestivalArtist(
        artistName = "The Moriah Sisters",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Indie / Folk"
    ),
    FestivalArtist(
        artistName = "Solya",
        stage = "T-Mobile",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Villanelle",
        stage = "American Express",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Jess Williamson",
        stage = "Miller Lite",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Folk / Alt-Country"
    ),
    FestivalArtist(
        artistName = "Aaron Rowe",
        stage = "BMI",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Britton",
        stage = "BeatBox",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Paloma Morphy",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Sunday (1994)",
        stage = "Snapchat",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Stella Lefty",
        stage = "T-Mobile",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Alternative Pop"
    ),
    FestivalArtist(
        artistName = "Dexter and the Moonrocks",
        stage = "American Express",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Alternative Rock / Country Rock"
    ),
    FestivalArtist(
        artistName = "Calder Allen",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Claire Rosinkranz",
        stage = "Miller Lite",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Fancy Hagood",
        stage = "BMI",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "underscores",
        stage = "BeatBox",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Hyperpop / Electropop"
    ),
    FestivalArtist(
        artistName = "Josh Conway",
        stage = "Snapchat",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Audrey Hobert",
        stage = "T-Mobile",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Rio Kosta",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Max McNown",
        stage = "American Express",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Saint Motel",
        stage = "Miller Lite",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Lauren Sanderson",
        stage = "BMI",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Alt Pop / R&B"
    ),
    FestivalArtist(
        artistName = "Noga Erez",
        stage = "BeatBox",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Alt Pop / Electronic / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Cannons",
        stage = "Snapchat",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Dream Pop / Synthpop"
    ),
    FestivalArtist(
        artistName = "Geese",
        stage = "T-Mobile",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "fcukers",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Electronic / Dance-punk"
    ),
    FestivalArtist(
        artistName = "SOFI TUKKER",
        stage = "American Express",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Indie Dance / House"
    ),
    FestivalArtist(
        artistName = "Parcels",
        stage = "Miller Lite",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Disco / Funk / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Blood Orange",
        stage = "BeatBox",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Alternative R&B / Indie Pop"
    ),
    FestivalArtist(
        artistName = "The War on Drugs",
        stage = "Snapchat",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Indie Rock / Heartland Rock"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "The xx",
        stage = "T-Mobile",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Indie Pop / Dream Pop"
    ),
    FestivalArtist(
        artistName = "Twenty One Pilots",
        stage = "American Express",
        day = "Weekend 1 — Sunday, October 4",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Alternative Rock / Hip-Hop / Pop"
    ),
    FestivalArtist(
        artistName = "Almost Heaven",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Happy Landing",
        stage = "T-Mobile",
        day = "Weekend 2 — Friday, October 9",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Indie Folk / Rock"
    ),
    FestivalArtist(
        artistName = "Brigitte Calls Me Baby",
        stage = "American Express",
        day = "Weekend 2 — Friday, October 9",
        startTime = "1:15 PM",
        endTime = "",
        genre = "New Wave / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Radio Free Alice",
        stage = "Miller Lite",
        day = "Weekend 2 — Friday, October 9",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Post-Punk / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Leon Knight",
        stage = "BMI",
        day = "Weekend 2 — Friday, October 9",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "S.G. Goodman",
        stage = "BeatBox",
        day = "Weekend 2 — Friday, October 9",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Americana / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Cassandra Coleman",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Singer-Songwriter / Indie"
    ),
    FestivalArtist(
        artistName = "Dallas Wax",
        stage = "Snapchat",
        day = "Weekend 2 — Friday, October 9",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Bella Kay",
        stage = "T-Mobile",
        day = "Weekend 2 — Friday, October 9",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Faouzia",
        stage = "American Express",
        day = "Weekend 2 — Friday, October 9",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Bo Staloch",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Sienna Spiro",
        stage = "Miller Lite",
        day = "Weekend 2 — Friday, October 9",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Girlfriend",
        stage = "BMI",
        day = "Weekend 2 — Friday, October 9",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Pop / Alternative"
    ),
    FestivalArtist(
        artistName = "World Famous Pets",
        stage = "BeatBox",
        day = "Weekend 2 — Friday, October 9",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "LP",
        stage = "Snapchat",
        day = "Weekend 2 — Friday, October 9",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Jesse Welles",
        stage = "T-Mobile",
        day = "Weekend 2 — Friday, October 9",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Folk Rock / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Natasha Bedingfield",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Amyl and the Sniffers",
        stage = "American Express",
        day = "Weekend 2 — Friday, October 9",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Punk Rock / Garage Punk"
    ),
    FestivalArtist(
        artistName = "Paris Paloma",
        stage = "Miller Lite",
        day = "Weekend 2 — Friday, October 9",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Joe Jordan",
        stage = "BMI",
        day = "Weekend 2 — Friday, October 9",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Singer-Songwriter / Rock"
    ),
    FestivalArtist(
        artistName = "Pilsowsky",
        stage = "BeatBox",
        day = "Weekend 2 — Friday, October 9",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "BUNT.",
        stage = "Snapchat",
        day = "Weekend 2 — Friday, October 9",
        startTime = "5:30 PM",
        endTime = "",
        genre = "House / Dance"
    ),
    FestivalArtist(
        artistName = "Turnstile",
        stage = "T-Mobile",
        day = "Weekend 2 — Friday, October 9",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Hardcore Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Steve Aoki",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Electro House / Big Room"
    ),
    FestivalArtist(
        artistName = "Labrinth",
        stage = "American Express",
        day = "Weekend 2 — Friday, October 9",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Alternative R&B / Electronic"
    ),
    FestivalArtist(
        artistName = "Leon Thomas",
        stage = "Miller Lite",
        day = "Weekend 2 — Friday, October 9",
        startTime = "7:15 PM",
        endTime = "",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "LIVE",
        stage = "BeatBox",
        day = "Weekend 2 — Friday, October 9",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Grunge"
    ),
    FestivalArtist(
        artistName = "The Chainsmokers",
        stage = "Snapchat",
        day = "Weekend 2 — Friday, October 9",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Electropop / Dance"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Friday, October 9",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "Kings of Leon",
        stage = "T-Mobile",
        day = "Weekend 2 — Friday, October 9",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Alternative Rock / Southern Rock"
    ),
    FestivalArtist(
        artistName = "Charli xcx",
        stage = "American Express",
        day = "Weekend 2 — Friday, October 9",
        startTime = "8:40 PM",
        endTime = "",
        genre = "Hyperpop / Electropop"
    ),
    FestivalArtist(
        artistName = "Macy Todd",
        stage = "BMI",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Pop / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Montclair",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Night Tapes",
        stage = "T-Mobile",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Synthpop / Dream Pop"
    ),
    FestivalArtist(
        artistName = "Annie DiRusso",
        stage = "American Express",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Indie Rock / Pop"
    ),
    FestivalArtist(
        artistName = "Temper City",
        stage = "Miller Lite",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Damaris Bojor",
        stage = "BMI",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Latin / Folk"
    ),
    FestivalArtist(
        artistName = "LLUVII",
        stage = "BeatBox",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Electronic / Indie"
    ),
    FestivalArtist(
        artistName = "Nat Myers",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Blues / Folk"
    ),
    FestivalArtist(
        artistName = "Gabriel Jacoby",
        stage = "Snapchat",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "2:00 PM",
        endTime = "",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "Balu Brigada",
        stage = "T-Mobile",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Finn Wolfhard",
        stage = "American Express",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Laszewo",
        stage = "Miller Lite",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "3:15 PM",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Don West",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Soul / R&B"
    ),
    FestivalArtist(
        artistName = "Common People",
        stage = "BMI",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie / Alternative"
    ),
    FestivalArtist(
        artistName = "Arcy Drive",
        stage = "BeatBox",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Rock / Garage Rock"
    ),
    FestivalArtist(
        artistName = "Skye Newman",
        stage = "Snapchat",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Pop / R&B"
    ),
    FestivalArtist(
        artistName = "Suki Waterhouse",
        stage = "T-Mobile",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Rodrigo y Gabriela",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Acoustic / Flamenco Rock"
    ),
    FestivalArtist(
        artistName = "Young Miko",
        stage = "American Express",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Latin Trap / Reggaeton"
    ),
    FestivalArtist(
        artistName = "Snow Strippers",
        stage = "Miller Lite",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Electroclash / Dance-pop"
    ),
    FestivalArtist(
        artistName = "Chloe Qisha",
        stage = "BMI",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Ryan Beatty",
        stage = "BeatBox",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Alternative R&B / Indie Pop"
    ),
    FestivalArtist(
        artistName = "It’s Murph",
        stage = "Snapchat",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "5:30 PM",
        endTime = "",
        genre = "House / Tech House"
    ),
    FestivalArtist(
        artistName = "Bleachers",
        stage = "T-Mobile",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "¥ØU\$UK€ ¥UK1MAT\$U",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Experimental Electronic / Techno"
    ),
    FestivalArtist(
        artistName = "Lola Young",
        stage = "American Express",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Alternative Pop / Soul"
    ),
    FestivalArtist(
        artistName = "Levity",
        stage = "Miller Lite",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "7:15 PM",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "fakemink",
        stage = "BeatBox",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Hip-Hop / Experimental Rap"
    ),
    FestivalArtist(
        artistName = "Lykke Li",
        stage = "Snapchat",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Indie Pop / Synthpop"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "Lorde",
        stage = "T-Mobile",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Art Pop / Electropop"
    ),
    FestivalArtist(
        artistName = "RÜFÜS DU SOL",
        stage = "American Express",
        day = "Weekend 2 — Saturday, October 10",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Indie Electronic / House"
    ),
    FestivalArtist(
        artistName = "MARZZ",
        stage = "BMI",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "12:45 PM",
        endTime = "",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "Huston-Tillotson University Jazz Collective",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Jazz"
    ),
    FestivalArtist(
        artistName = "Thomas Day",
        stage = "T-Mobile",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Rum Jungle",
        stage = "American Express",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "Joshua Jensen",
        stage = "Miller Lite",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Singer-Songwriter / Indie"
    ),
    FestivalArtist(
        artistName = "Chelsea Jordan",
        stage = "BMI",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Country / Pop"
    ),
    FestivalArtist(
        artistName = "Kevin Atwater",
        stage = "BeatBox",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Folk / Singer-Songwriter"
    ),
    FestivalArtist(
        artistName = "Paloma Morphy",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Sunday (1994)",
        stage = "Snapchat",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Indie Pop / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Charlotte Lawrence",
        stage = "T-Mobile",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Pop"
    ),
    FestivalArtist(
        artistName = "Ethan Regan",
        stage = "American Express",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Indie Rock / Folk"
    ),
    FestivalArtist(
        artistName = "Calder Allen",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Claire Rosinkranz",
        stage = "Miller Lite",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "VWILLZ",
        stage = "BMI",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Bad Nerves",
        stage = "BeatBox",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Punk / Power Pop"
    ),
    FestivalArtist(
        artistName = "Grace Ives",
        stage = "Snapchat",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Synthpop / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Audrey Hobert",
        stage = "T-Mobile",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Indie Pop"
    ),
    FestivalArtist(
        artistName = "Rio Kosta",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Max McNown",
        stage = "American Express",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Country / Folk"
    ),
    FestivalArtist(
        artistName = "Saint Motel",
        stage = "Miller Lite",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Indie Pop / Rock"
    ),
    FestivalArtist(
        artistName = "Noga Erez",
        stage = "BeatBox",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Alt Pop / Electronic / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Houndmouth",
        stage = "Snapchat",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Americana / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Sasha Keable",
        stage = "BMI",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "6:00 PM",
        endTime = "",
        genre = "R&B / Soul"
    ),
    FestivalArtist(
        artistName = "Geese",
        stage = "T-Mobile",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Indie Rock"
    ),
    FestivalArtist(
        artistName = "fcukers",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Electronic / Dance-punk"
    ),
    FestivalArtist(
        artistName = "SOFI TUKKER",
        stage = "American Express",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "6:30 PM",
        endTime = "",
        genre = "Indie Dance / House"
    ),
    FestivalArtist(
        artistName = "Parcels",
        stage = "Miller Lite",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Disco / Funk / Indie Pop"
    ),
    FestivalArtist(
        artistName = "Blood Orange",
        stage = "BeatBox",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Alternative R&B / Indie Pop"
    ),
    FestivalArtist(
        artistName = "The War on Drugs",
        stage = "Snapchat",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Indie Rock / Heartland Rock"
    ),
    FestivalArtist(
        artistName = "Silent Disco",
        stage = "Tito’s Handmade Vodka",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Dance / DJ"
    ),
    FestivalArtist(
        artistName = "The xx",
        stage = "T-Mobile",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Indie Pop / Dream Pop"
    ),
    FestivalArtist(
        artistName = "Twenty One Pilots",
        stage = "American Express",
        day = "Weekend 2 — Sunday, October 11",
        startTime = "8:30 PM",
        endTime = "",
        genre = "Alternative Rock / Hip-Hop / Pop"
    ),
)

// ============================================================
// Life is Beautiful (NV) — 0 verified entries
// No official 2026 dates or lineup found as of Aug. 26, 2026; do not use third-party speculative/fake lineup pages.
// ============================================================
val lifeIsBeautiful2026 = emptyList<FestivalArtist>()

// ============================================================
// Lost Lands (OH) — 209 verified entries
// Verified 2026 artist roster. Lost Lands’ official lineup page is poster/image based. Current schedule indexes still say official 2026 set times have not been released, so stage/day/start/end are intentionally blank.
// ============================================================
val lostLands2026 = listOf(
    FestivalArtist(
        artistName = "Adventure Club",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "ÆON:MODE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "ALLEYCVT",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "ARMNHMR",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "ATLiens",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Audiofreq",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Hard Dance / Hardcore"
    ),
    FestivalArtist(
        artistName = "Barely Alive",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Bear Grillz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Benda",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Blossom",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House / House"
    ),
    FestivalArtist(
        artistName = "Boogie T",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Borgore",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Bou",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Calcium",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Riddim"
    ),
    FestivalArtist(
        artistName = "Canabliss",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Caspa",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Crankdat",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Craze",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Turntablism / Bass / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Culture Shock",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Cyclops",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Delta Heavy",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Dieselboy",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Dion Timmer",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dirt Monkey",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dirtyphonics",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Distinct Motive",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Doctor P",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dr. Fresch",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "DRINKURWATER",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Effin",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Emorfik",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Eptic",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Excision presents: 2 Hour Set",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Excision presents: Detox Set",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Excision B2B SPACE LACES",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Flosstradamus",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Flux Pavilion",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "FuntCase",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ganja White Night",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ghastly",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House / Dubstep"
    ),
    FestivalArtist(
        artistName = "GHENGAR",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "gladde paling",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Glitch / Breakcore / Electronic"
    ),
    FestivalArtist(
        artistName = "Grabbitz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Electronic Rock / Bass"
    ),
    FestivalArtist(
        artistName = "Hairitage",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Hedex",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "HEYZ",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "HOL!",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "ILLENIUM",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "INFEKT",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Ivy Lab",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Jantsen",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Jessica Audiffred",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Jkyl & Hyde",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Kai Wachi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Know Good",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Kompany",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Krewella",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Electropop / Bass"
    ),
    FestivalArtist(
        artistName = "LAYZ",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Level Up",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Levity",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Lil Texas",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Hard Dance / Hardcore"
    ),
    FestivalArtist(
        artistName = "Liquid Stranger",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "LYNY",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Mefjus",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "NGHTMRE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Oliverse",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Passport",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "PhaseOne",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Metal"
    ),
    FestivalArtist(
        artistName = "Ravenscoon",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Ray Volpe",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "REAPER",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "THE RESISTANCE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Riot Ten",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Samplifire",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Seven Lions",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "Sigma",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "SIPPY",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SLANDER",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "Smoakland",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SoDown",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Electro-soul / Bass"
    ),
    FestivalArtist(
        artistName = "Stumpi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House"
    ),
    FestivalArtist(
        artistName = "Subtronics",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Sullivan King",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Metal"
    ),
    FestivalArtist(
        artistName = "Taiki Nulight",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "Trivecta",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "TRUTH",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Virtual Riot",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Wax Motif",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass House / Tech House"
    ),
    FestivalArtist(
        artistName = "Whethan",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Future Bass / Electronic"
    ),
    FestivalArtist(
        artistName = "The Widdler",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "William Black",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "WonkyWilla",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Wooli",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "YOOKiE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Zingara",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Zomboy",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "\$J",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "2DY4",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "AlienPark",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "All The Reason",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Arlo",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Au5",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Austeria",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "AVELLO",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "BadKlaat",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Basstripper",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Bella Renee",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Bass / Vocal Electronic"
    ),
    FestivalArtist(
        artistName = "Big Florida",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Brainrack",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Capochino",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Casey Club",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Champagne Drip",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Chassi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Chozen",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Codd Dubz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Crizzly",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Crumb Pit",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Crystal Skies",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "Darksiderz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Hard Dance / Hardcore"
    ),
    FestivalArtist(
        artistName = "Deadcrow",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "DirtySnatcha",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Distant Matter",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Dodge & Fuski",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dr. Ushūu",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dream Takers",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Dubscribe",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Finnuh",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "FUTURE EXIT",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Gardella",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Green Matter",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "HALIENE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trance / Melodic Bass Vocals"
    ),
    FestivalArtist(
        artistName = "HerShe",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Hostage Situation",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "HURTBOX",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "HVDES",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dark Electronic / Bass"
    ),
    FestivalArtist(
        artistName = "Hydraulix",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "IMANU",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Ivory",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Izadi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Izzy Vadim",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Jaenga",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Josh Teed",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Violin / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Killmatter",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Kliptic",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Klo",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Lazrus",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Leotrix",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Lowcation",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Luci",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Electronic"
    ),
    FestivalArtist(
        artistName = "Lumasi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Machaki",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mad Dubz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "MADGRRL",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Hard Dance / Bass"
    ),
    FestivalArtist(
        artistName = "Mile32",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mindset",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Modal Nodes",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mozey",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Mport",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Muerte",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Myrias",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "MYTHM",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Neotek",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Neumonic",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Nikita, the Wicked",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Nimda",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Noetika",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "OG Nixin",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Onara",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Paper Skies",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Pegboard Nerds",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Phrva",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Poni",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Pretty Sweet",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "ProbCause",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Prosecute",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "RemK",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Richard Finger",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Roi",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "rSUN",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ryns",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "RZRKT",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Saint Miller",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Seth David",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Shlump",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SISTO",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Skilah",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Space Wizard",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SPORTMODE",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "SQISHI",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Stoned Level",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Subsonic",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Super Future",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Tisoki",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Tokyo Machine",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Electro House / Bass"
    ),
    FestivalArtist(
        artistName = "Twopercent",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "TYNAN",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Usaybflow",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "VAMPA",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "VKTM",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Warlord",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Whales",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Wiley",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Wraz",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Xotix",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "yetep",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Melodic Dubstep / Future Bass"
    ),
    FestivalArtist(
        artistName = "yvm3",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Zen Selekta",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "ZERO (UK)",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Zoey808",
        stage = "",
        day = "",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
)

// ============================================================
// Burning Man (NV) — 0 verified entries
// Burning Man does not publish a centralized official music lineup: camps/art cars book independently and Burning Man explicitly says there is too much music to assemble into one official list. No rumor/volunteer guide data is inserted here.
// ============================================================
val burningMan2026 = emptyList<FestivalArtist>()

// ============================================================
// Dancefestopia (KS) — 173 verified entries
// Dancefestopia’s official site confirms 250+ artists/five stages and says set times are out, but the exact current set-time grid is graphic-only. This list uses the current day-by-day performance roster, with Emerald/Lollipop assignments only where Dancefestopia itself published the stage artist lists. Unknown Forest/Pool/ReKinection assignments and exact times remain blank rather than guessed.
// ============================================================
val dancefestopia2026 = listOf(
    FestivalArtist(
        artistName = "Radikill",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Scag Dubz",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Bon Panda Breaks",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Breaks / Bass"
    ),
    FestivalArtist(
        artistName = "Field\$",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Gonza",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "DJ Ortega",
        stage = "",
        day = "Monday, September 7",
        startTime = "",
        endTime = "",
        genre = "Electronic / Bass"
    ),
    FestivalArtist(
        artistName = "Choic3",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Demigod",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Will Janklow",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mempo B2B Conflikt",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "B Marsh",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Phantum",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ambersnow",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Seda",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Lahloh",
        stage = "",
        day = "Tuesday, September 8",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Ryno B2B Team Daniel",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Overcast",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Moonbound",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Golden Goddess",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Insison",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Filthy Trace",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Clvrk Kent",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Temple",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Dub Trio",
        stage = "",
        day = "Wednesday, September 9",
        startTime = "",
        endTime = "",
        genre = "Dub / Experimental Rock"
    ),
    FestivalArtist(
        artistName = "Philthy B2B Hope Circuit",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Balance B2B Mumbo",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "14All Fam",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Sleeper B2B Thresh",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Skrrt Cobain",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Dreamzzz",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Unfettered",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Bvssbratt",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Paper Skies",
        stage = "Emerald Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Fractal Bloom",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ryan Richardson",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Lumasi",
        stage = "Emerald Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dayzero",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Effin",
        stage = "Emerald Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Nmezee",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Big Dyl B2B Dr3vd Nox",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ray Volpe",
        stage = "Emerald Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Mike Ho B2B Hooplah",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "CRANKDAT",
        stage = "Emerald Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Psilly B2B Star Complex",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Phantom Operator",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Rüger",
        stage = "",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Grabbitz",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Electronic Rock / Bass"
    ),
    FestivalArtist(
        artistName = "MAD DUBZ",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "HEXXA",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ozztin",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Chmura",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Austeria",
        stage = "Lollipop Stage",
        day = "Thursday, September 10",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mycelium",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Anti Plastic",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Litebug",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Nowhere Further",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Ncite",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Kota Who?",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mther",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Manipadme",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Riddim Slinger B2B Bluff Baby",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Megatron B2B Kxiti",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "N8vboy",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Elias True",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Subrosa...",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Izzy Vadim",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Haijack B2B Piknik",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Jaenga",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Mermix",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Bleach",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "WonkyWilla",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Sharker",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Journey Jones",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Blare",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Know Good",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "B!gmac B2B Meteorik",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "D.Mic",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "EAZYBAKED",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Rüger B2B Darkwood",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Wreckno",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Hip-hop"
    ),
    FestivalArtist(
        artistName = "Dawni",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Air Quotes B2B ItsNotImportant",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Subplay",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SCSI",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Acrylik B2B ANJ",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "LSDREAM",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Sugar Drip",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "The Rico Suave",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Hokage B2B Slayday",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "GRiZ",
        stage = "Emerald Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Funk / Electro-soul / Bass"
    ),
    FestivalArtist(
        artistName = "Mark OG'",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Grinz B2B Ginja Ninja",
        stage = "",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Mushroom Cloud",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "INFEKT",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Phrva",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Star Monster",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Pretty Sweet",
        stage = "Lollipop Stage",
        day = "Friday, September 11",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Banditz",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Apacolypto",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Rais3r",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Risa",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Proper Grammar",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Alil",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Subrosa...",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Madnoiz B2B Slabb",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "DREAM & FRIENDS FT. SHARKER, JIMMICK, AND BAGZ",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "ETRNL B2B Pandicorn",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "G@lxy",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "TYNAN",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Trap / Bass"
    ),
    FestivalArtist(
        artistName = "Panda",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "SMOAKLAND",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Vincït",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Hostile",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "REAPER",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Drum & Bass"
    ),
    FestivalArtist(
        artistName = "Half Moon",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Darkwood B2B Callisto",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Rise B2B Bagz",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Heyz",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Lektrik B2B Sheppa",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Zero One",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Layz",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Y'all Thought B2B Txana",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Alleycvt",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Dirty Vacation B2B Imposter Sindrum",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Cinimod",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Buck Norris",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Deluluz",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Elixa B2B King Coopa",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Sullivan King",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Metal"
    ),
    FestivalArtist(
        artistName = "Vis!ons",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Savage Habits B2B Botz & Bandz",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Excision",
        stage = "Emerald Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Illite",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Oliverse",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Mob Boss B2B V Tach",
        stage = "",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Kompany",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Calcium",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Riddim"
    ),
    FestivalArtist(
        artistName = "Mport",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Just A Gent",
        stage = "Lollipop Stage",
        day = "Saturday, September 12",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Jaywalk",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "User00215",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Yaws",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Nick Niemeier",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Hellaquent",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Aliza",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Slvr Fox",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Orb.it",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Kyokee",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Nofslinger",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Døwn Two Freaks",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Steller",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Just Tommy",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "ProbCause",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Indigenous",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Ncite",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Jkyl & Hyde",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Babysox",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "FNU B2B Axe6",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Sippy",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Spenny",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Lazuli",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Ravenscoon",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Human Penguin B2B Saul Gucci",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass Music / Dubstep"
    ),
    FestivalArtist(
        artistName = "Scum Wubz B2B Larj",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Habrin",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Inzo",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Bass / Experimental Bass"
    ),
    FestivalArtist(
        artistName = "Blaqout",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Rissross",
        stage = "",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Zeds Dead",
        stage = "Emerald Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
    FestivalArtist(
        artistName = "Riot Ten B2B Bear Grillz",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "SampliFire",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "USAYBFLOW",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Riddim / Dubstep"
    ),
    FestivalArtist(
        artistName = "Eliminate",
        stage = "Lollipop Stage",
        day = "Sunday, September 13",
        startTime = "",
        endTime = "",
        genre = "Dubstep / Bass Music"
    ),
)

// ============================================================
// Aftershock (CA) — 143 verified entries
// Current 2026 daily stage schedule published Aug. 18. Start times are populated; end times remain blank because the published textual schedule does not provide exact ends. The official Twitch-winner placeholder is preserved as a placeholder.
// ============================================================
val aftershock2026 = listOf(
    FestivalArtist(
        artistName = "Free Throw",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "11:55 AM",
        endTime = "",
        genre = "Emo / Indie Rock"
    ),
    FestivalArtist(
        artistName = "Lit",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "We The Kings",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Mayday Parade",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Pop Punk / Emo"
    ),
    FestivalArtist(
        artistName = "Hot Mulligan",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Emo / Pop Punk"
    ),
    FestivalArtist(
        artistName = "Sublime",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "6:20 PM",
        endTime = "",
        genre = "Alternative Rock / Reggae Rock"
    ),
    FestivalArtist(
        artistName = "My Chemical Romance",
        stage = "Aftershock Stage",
        day = "Thursday, October 1",
        startTime = "8:25 PM",
        endTime = "",
        genre = "Emo / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Leap",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "L.S. Dunes",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Hawthorne Heights",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "1:25 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Starting Line",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "2:35 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "New Found Glory",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "3:55 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "The Used",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Offspring",
        stage = "Shockwave",
        day = "Thursday, October 1",
        startTime = "7:20 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "The Violent Hour",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Hard Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Red",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "1:05 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Finger Eleven",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "2:10 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Grunge"
    ),
    FestivalArtist(
        artistName = "Apocalyptica",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Classical Crossover / Symphonic Metal"
    ),
    FestivalArtist(
        artistName = "Theory Of A Deadman",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "4:25 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Grunge"
    ),
    FestivalArtist(
        artistName = "Nothing More",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "5:40 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "The Pretty Reckless",
        stage = "The Point Stage Presented By Coors Light",
        day = "Thursday, October 1",
        startTime = "7:35 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Primer 55",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "11:35 AM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Brujeria",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "12:35 PM",
        endTime = "",
        genre = "Extreme Metal / Grindcore"
    ),
    FestivalArtist(
        artistName = "Ünloco",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "1:40 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Ill Niño",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "The Union Underground",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "3:55 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Coal Chamber",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "5:10 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Sevendust",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "6:45 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Killswitch Engage",
        stage = "Faultline",
        day = "Thursday, October 1",
        startTime = "8:20 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Wind Walkers",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "The Word Alive",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Caskets",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Holding Absence",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "From Ashes To New",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "6:20 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Starset",
        stage = "Epicenter",
        day = "Thursday, October 1",
        startTime = "9:10 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Silly Goose",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Nu Metal / Rap Metal"
    ),
    FestivalArtist(
        artistName = "Drowning Pool",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "P.O.D.",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "1:20 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Insane Clown Posse",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Horrorcore Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Cypress Hill",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Public Enemy",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "5:55 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Wu-Tang Clan",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "7:35 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Limp Bizkit",
        stage = "Aftershock Stage",
        day = "Friday, October 2",
        startTime = "9:45 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Haarper",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "11:55 AM",
        endTime = "",
        genre = "Horrorcore / Trap"
    ),
    FestivalArtist(
        artistName = "Haywire",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Drain",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Kublai Khan TX",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Three 6 Mafia",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "5:05 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Slaughter To Prevail",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "6:45 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "\$uicideboy\$",
        stage = "Shockwave",
        day = "Friday, October 2",
        startTime = "8:40 PM",
        endTime = "",
        genre = "Emo Rap / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Eyes Set To Kill",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "12:45 PM",
        endTime = "",
        genre = "Post-Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Alesana",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Finch",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Atreyu",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "3:50 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Senses Fail",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "5:00 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Alexisonfire",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "6:10 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Underoath",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "7:20 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Coheed And Cambria",
        stage = "The Point Stage Presented By Coors Light",
        day = "Friday, October 2",
        startTime = "8:50 PM",
        endTime = "",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Vianova",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Progressive Metalcore"
    ),
    FestivalArtist(
        artistName = "Psychostick",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "2:15 PM",
        endTime = "",
        genre = "Comedy Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Nekrogoblikon",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "3:20 PM",
        endTime = "",
        genre = "Folk / Experimental Metal"
    ),
    FestivalArtist(
        artistName = "Blue Medusa feat. Alissa White-Gluz",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Heavy Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "Cradle Of Filth",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "5:35 PM",
        endTime = "",
        genre = "Black Metal / Gothic Metal"
    ),
    FestivalArtist(
        artistName = "Chad Gray",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "8:00 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Dethklok",
        stage = "Faultline",
        day = "Friday, October 2",
        startTime = "9:45 PM",
        endTime = "",
        genre = "Industrial Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Rivers Of Nihil",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Progressive Death Metal"
    ),
    FestivalArtist(
        artistName = "Peelingflesh",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "1:20 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Bodysnatcher",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Spite",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "The Black Dahlia Murder",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "5:55 PM",
        endTime = "",
        genre = "Melodic Death Metal"
    ),
    FestivalArtist(
        artistName = "Paleface Swiss",
        stage = "Epicenter",
        day = "Friday, October 2",
        startTime = "7:40 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Fear, And Loathing In Las Vegas",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Electronicore / Metalcore"
    ),
    FestivalArtist(
        artistName = "ivri",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "12:30 PM",
        endTime = "",
        genre = "Alternative Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Blessthefall",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "1:30 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Set It Off",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Chiodos",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "4:05 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Sleeping With Sirens",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "5:35 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Babymetal",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "7:20 PM",
        endTime = "",
        genre = "Kawaii Metal / J-Metal"
    ),
    FestivalArtist(
        artistName = "Pierce The Veil",
        stage = "Aftershock Stage",
        day = "Saturday, October 3",
        startTime = "9:25 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Austin Carlile",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "12:00 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Escape The Fate",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "The Devil Wears Prada",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "The Wonder Years",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "3:25 PM",
        endTime = "",
        genre = "Pop Punk / Emo"
    ),
    FestivalArtist(
        artistName = "Wage War",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "4:50 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "The Story So Far",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "6:25 PM",
        endTime = "",
        genre = "Pop Punk"
    ),
    FestivalArtist(
        artistName = "A Day To Remember",
        stage = "Shockwave",
        day = "Saturday, October 3",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Easycore / Metalcore"
    ),
    FestivalArtist(
        artistName = "TBD — Twitch Winner",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "12:30 PM",
        endTime = "",
        genre = ""
    ),
    FestivalArtist(
        artistName = "Doobie",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "1:30 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "Pentagram",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Doom Metal"
    ),
    FestivalArtist(
        artistName = "Melvins",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "3:45 PM",
        endTime = "",
        genre = "Sludge / Stoner Metal"
    ),
    FestivalArtist(
        artistName = "Kylesa",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "5:00 PM",
        endTime = "",
        genre = "Sludge / Stoner Metal"
    ),
    FestivalArtist(
        artistName = "Corrosion Of Conformity",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "6:20 PM",
        endTime = "",
        genre = "Sludge / Stoner Metal"
    ),
    FestivalArtist(
        artistName = "Down",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "7:45 PM",
        endTime = "",
        genre = "Sludge / Stoner Metal"
    ),
    FestivalArtist(
        artistName = "Black Label Society",
        stage = "The Point Stage Presented By Coors Light",
        day = "Saturday, October 3",
        startTime = "9:40 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "The Fall Of Troy",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Post-Hardcore / Math Rock"
    ),
    FestivalArtist(
        artistName = "Emery",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Horse The Band",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "1:10 PM",
        endTime = "",
        genre = "Nintendocore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Armor For Sleep",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Emo / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Saosin",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "3:00 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Thursday",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "La Dispute",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "5:45 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Circa Survive",
        stage = "Faultline",
        day = "Saturday, October 3",
        startTime = "7:10 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Gideon",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "12:10 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Stick To Your Guns",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "1:20 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "The Acacia Strain",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "2:40 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "After The Burial",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "4:00 PM",
        endTime = "",
        genre = "Technical / Progressive Deathcore"
    ),
    FestivalArtist(
        artistName = "Counterparts",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "The Ghost Inside",
        stage = "Epicenter",
        day = "Saturday, October 3",
        startTime = "7:15 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "CKY",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "11:55 AM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Helmet",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Buckethead",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Instrumental Rock / Experimental"
    ),
    FestivalArtist(
        artistName = "Zakk Sabbath",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "3:20 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "AFI",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Danny Elfman",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Rock / Orchestral / Experimental"
    ),
    FestivalArtist(
        artistName = "Tool",
        stage = "Aftershock Stage",
        day = "Sunday, October 4",
        startTime = "8:25 PM",
        endTime = "",
        genre = "Progressive Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Toadies",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Oleander",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Filter",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "1:25 PM",
        endTime = "",
        genre = "Industrial Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Wolfmother",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Highly Suspect",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "4:00 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Stone Temple Pilots",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "5:25 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Queens of the Stone Age",
        stage = "Shockwave",
        day = "Sunday, October 4",
        startTime = "7:20 PM",
        endTime = "",
        genre = "Alternative Rock / Stoner Rock"
    ),
    FestivalArtist(
        artistName = "Codefendants",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Punk / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Cro-Mags",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "1:25 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Municipal Waste",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "2:45 PM",
        endTime = "",
        genre = "Thrash Metal"
    ),
    FestivalArtist(
        artistName = "Soulfly",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "3:55 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "Cavalera",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "4:55 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "Suicidal Tendencies",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "6:10 PM",
        endTime = "",
        genre = "Crossover Thrash / Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Body Count",
        stage = "The Point Stage Presented By Coors Light",
        day = "Sunday, October 4",
        startTime = "7:35 PM",
        endTime = "",
        genre = "Rap Metal / Hardcore"
    ),
    FestivalArtist(
        artistName = "I Set My Friends On Fire",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "11:55 AM",
        endTime = "",
        genre = "Post-Hardcore / Electronicore"
    ),
    FestivalArtist(
        artistName = "Emarosa",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Drop Dead, Gorgeous",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Post-Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "I See Stars",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "2:40 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Hail The Sun",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "3:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "From First To Last",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Home Team",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "6:55 PM",
        endTime = "",
        genre = "Pop Rock / Alternative"
    ),
    FestivalArtist(
        artistName = "Dance Gavin Dance",
        stage = "Faultline",
        day = "Sunday, October 4",
        startTime = "8:10 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Cenobia",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "The Pretty Wild",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Alternative Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "Holywatr",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "3:20 PM",
        endTime = "",
        genre = "Alternative Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Jutes",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "4:45 PM",
        endTime = "",
        genre = "Alternative Rock / Pop"
    ),
    FestivalArtist(
        artistName = "President",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "6:10 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Sleep Theory",
        stage = "Epicenter",
        day = "Sunday, October 4",
        startTime = "9:10 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
)

// ============================================================
// Electric Zoo (NY) — 0 verified entries
// No confirmed 2026 Electric Zoo edition, dates, operator, lineup, stages, or set times as of Aug. 26, 2026. Third-party event listings are not treated as official.
// ============================================================
val electricZoo2026 = emptyList<FestivalArtist>()

// ============================================================
// Louder Than Life (KY) — 198 verified entries
// Current 2026 daily schedule released Aug. 14. Start times/stages are populated; exact end times are blank because the available textual schedule gives starts. The Friday Impact Stage TBA is preserved as an official placeholder.
// ============================================================
val louderThanLife2026 = listOf(
    FestivalArtist(
        artistName = "Iron Maiden",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "8:45 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "Megadeth",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "6:35 PM",
        endTime = "",
        genre = "Thrash Metal"
    ),
    FestivalArtist(
        artistName = "Alice Cooper",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "4:45 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "Anthrax",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Thrash Metal"
    ),
    FestivalArtist(
        artistName = "Suicidal Tendencies",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "1:55 PM",
        endTime = "",
        genre = "Crossover Thrash / Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Metal Church",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "12:50 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "The Violent Hour",
        stage = "Louder Stage",
        day = "Thursday, September 17",
        startTime = "11:55 AM",
        endTime = "",
        genre = "Hard Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Pantera",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "Danzig",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "5:40 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "Sabaton",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "4:00 PM",
        endTime = "",
        genre = "Power Metal"
    ),
    FestivalArtist(
        artistName = "Machine Head",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "GWAR",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "1:20 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "Mac Sabbath",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "12:20 PM",
        endTime = "",
        genre = "Comedy Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Chained Saint",
        stage = "Life Stage",
        day = "Thursday, September 17",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Heavy Metal / Thrash Metal"
    ),
    FestivalArtist(
        artistName = "Rise Against",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "10:05 PM",
        endTime = "",
        genre = "Punk Rock / Melodic Hardcore"
    ),
    FestivalArtist(
        artistName = "Jimmy Eat World",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Alternative Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Hot Mulligan",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "6:35 PM",
        endTime = "",
        genre = "Emo / Pop Punk"
    ),
    FestivalArtist(
        artistName = "Alkaline Trio",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "5:05 PM",
        endTime = "",
        genre = "Punk Rock / Emo"
    ),
    FestivalArtist(
        artistName = "Bowling For Soup",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "3:50 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Lit",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "2:40 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "The Ataris",
        stage = "Decibel Stage",
        day = "Thursday, September 17",
        startTime = "1:30 PM",
        endTime = "",
        genre = "Pop Punk"
    ),
    FestivalArtist(
        artistName = "Skillet",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "9:10 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Starset",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "7:25 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "From Ashes To New",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "5:50 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Red",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "The Rasmus",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Adelitas Way",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Zero 9:36",
        stage = "Reverb Stage",
        day = "Thursday, September 17",
        startTime = "12:55 PM",
        endTime = "",
        genre = "Rap Rock / Alternative"
    ),
    FestivalArtist(
        artistName = "Fit For A King",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "10:05 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Currents",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "ERRA",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "6:35 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Volumes",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "5:20 PM",
        endTime = "",
        genre = "Progressive Metalcore"
    ),
    FestivalArtist(
        artistName = "Like Moths To Flames",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "3:55 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Cane Hill",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "2:35 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Vianova",
        stage = "Loudmouth Stage",
        day = "Thursday, September 17",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Progressive Metalcore"
    ),
    FestivalArtist(
        artistName = "Chelsea Grin",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "9:10 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "After The Burial",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "5:50 PM",
        endTime = "",
        genre = "Technical / Progressive Deathcore"
    ),
    FestivalArtist(
        artistName = "The Acacia Strain",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "4:35 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Emmure",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Born Of Osiris",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "2:05 PM",
        endTime = "",
        genre = "Technical / Progressive Deathcore"
    ),
    FestivalArtist(
        artistName = "Signs Of The Swarm",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "King 810",
        stage = "Impact Stage",
        day = "Thursday, September 17",
        startTime = "12:00 PM",
        endTime = "",
        genre = "Nu Metal / Metalcore"
    ),
    FestivalArtist(
        artistName = "Thousand Below",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "7:20 PM",
        endTime = "",
        genre = "Post-Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Archers",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "5:20 PM",
        endTime = "",
        genre = "Metalcore / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Elijah",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "4:25 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Dark Divine",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "3:00 PM",
        endTime = "",
        genre = "Metalcore / Horror Rock"
    ),
    FestivalArtist(
        artistName = "Holy Wars",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "1:30 PM",
        endTime = "",
        genre = "Alternative Metal / Industrial Rock"
    ),
    FestivalArtist(
        artistName = "Sent By Ravens",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "12:30 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Set For Tomorrow",
        stage = "Big Bourbon Bar",
        day = "Thursday, September 17",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Metalcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "My Chemical Romance",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "9:25 PM",
        endTime = "",
        genre = "Emo / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "A Day To Remember",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "7:10 PM",
        endTime = "",
        genre = "Easycore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Taking Back Sunday",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Emo / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Coheed And Cambria",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "3:50 PM",
        endTime = "",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "We The Kings",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "2:25 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "L.S. Dunes",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Red Jumpsuit Apparatus",
        stage = "Louder Stage",
        day = "Friday, September 18",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Pierce The Veil",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Used",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "6:20 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Sleeping With Sirens",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Loathe",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "3:05 PM",
        endTime = "",
        genre = "Alternative Metal / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Get Scared",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "1:50 PM",
        endTime = "",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Lacey Sturm",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "ivri",
        stage = "Life Stage",
        day = "Friday, September 18",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Alternative Rock / Emo"
    ),
    FestivalArtist(
        artistName = "The Pretty Reckless",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "10:00 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "The Warning",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Dead Poet Society",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "6:25 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Scene Queen",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Metalcore / Bimbocore"
    ),
    FestivalArtist(
        artistName = "Vana",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "4:05 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Showing Teeth",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "2:55 PM",
        endTime = "",
        genre = "Hard Rock / Metal"
    ),
    FestivalArtist(
        artistName = "American Monster",
        stage = "Decibel Stage",
        day = "Friday, September 18",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Hard Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Chad Gray",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "9:05 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Cavalera",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "7:25 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "Blue Medusa feat. Alissa White-Gluz",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "5:50 PM",
        endTime = "",
        genre = "Heavy Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "Soulfly",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "4:40 PM",
        endTime = "",
        genre = "Groove Metal"
    ),
    FestivalArtist(
        artistName = "Mushroomhead",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "3:30 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Butcher Babies",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "2:20 PM",
        endTime = "",
        genre = "Groove Metal / Metalcore"
    ),
    FestivalArtist(
        artistName = "Death Valley Dreams",
        stage = "Reverb Stage",
        day = "Friday, September 18",
        startTime = "1:10 PM",
        endTime = "",
        genre = "Alternative Rock / Electronic Rock"
    ),
    FestivalArtist(
        artistName = "President",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "6:25 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "Holding Absence",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "5:25 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "The Word Alive",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "4:30 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Rain City Drive",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "3:35 PM",
        endTime = "",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Wind Walkers",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "2:40 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Nevertel",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "1:35 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Cenobia",
        stage = "Loudmouth Stage",
        day = "Friday, September 18",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Alternative Metal / Rock"
    ),
    FestivalArtist(
        artistName = "TBA",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "8:15 PM",
        endTime = "",
        genre = ""
    ),
    FestivalArtist(
        artistName = "Haywire",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "6:50 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Angel Du\$t",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "5:55 PM",
        endTime = "",
        genre = "Hardcore Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Twitching Tongues",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "5:00 PM",
        endTime = "",
        genre = "Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Koyo",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "4:05 PM",
        endTime = "",
        genre = "Melodic Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Missing Link",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "3:10 PM",
        endTime = "",
        genre = "Hardcore / Metal"
    ),
    FestivalArtist(
        artistName = "Gates To Hell",
        stage = "Impact Stage",
        day = "Friday, September 18",
        startTime = "2:15 PM",
        endTime = "",
        genre = "Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "SOiL",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "7:00 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Jeff Hardy",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "5:00 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Dry Kill Logic",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "4:05 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Primer 55",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "2:55 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Earshot",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "2:00 PM",
        endTime = "",
        genre = "Alternative Metal / Post-Grunge"
    ),
    FestivalArtist(
        artistName = "40 Below Summer",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "1:10 PM",
        endTime = "",
        genre = "Nu Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Billy McNicol",
        stage = "Big Bourbon Bar",
        day = "Friday, September 18",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Limp Bizkit",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "9:45 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Sublime",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "7:30 PM",
        endTime = "",
        genre = "Alternative Rock / Reggae Rock"
    ),
    FestivalArtist(
        artistName = "Bilmuri",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "5:45 PM",
        endTime = "",
        genre = "Alternative Rock / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Tom Morello",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "4:05 PM",
        endTime = "",
        genre = "Rap Rock / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "P.O.D.",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "2:30 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Alien Ant Farm",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Josey Scott: The Original Voice Of Saliva",
        stage = "Louder Stage",
        day = "Saturday, September 19",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Nu Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "Papa Roach",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "8:25 PM",
        endTime = "",
        genre = "Rap Rock / Nu Metal"
    ),
    FestivalArtist(
        artistName = "Babymetal",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "6:35 PM",
        endTime = "",
        genre = "Kawaii Metal / J-Metal"
    ),
    FestivalArtist(
        artistName = "Ice Nine Kills",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "4:55 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Nothing More",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "3:15 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Set It Off",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "1:50 PM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "The Funeral Portrait",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Emo / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "TX2",
        stage = "Life Stage",
        day = "Saturday, September 19",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Pop Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Halestorm",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "7:45 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "In This Moment",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "6:05 PM",
        endTime = "",
        genre = "Alternative Metal / Metalcore"
    ),
    FestivalArtist(
        artistName = "Lindsey Stirling",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "4:25 PM",
        endTime = "",
        genre = "Classical Crossover / Symphonic Metal"
    ),
    FestivalArtist(
        artistName = "Orianthi",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "3:00 PM",
        endTime = "",
        genre = "Hard Rock"
    ),
    FestivalArtist(
        artistName = "Icon For Hire",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "1:50 PM",
        endTime = "",
        genre = "Alternative Rock / Electronic Rock"
    ),
    FestivalArtist(
        artistName = "Kami Kehoe",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Alternative Rock / Pop"
    ),
    FestivalArtist(
        artistName = "Diamante",
        stage = "Decibel Stage",
        day = "Saturday, September 19",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Hard Rock / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Circa Survive",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "8:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Dance Gavin Dance",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "6:55 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Chiodos",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Thursday",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "3:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Hail The Sun",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "2:25 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Scary Kids Scaring Kids",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Emarosa",
        stage = "Reverb Stage",
        day = "Saturday, September 19",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Kublai Khan TX",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "7:35 PM",
        endTime = "",
        genre = "Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Boundaries",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "6:25 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Spite",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "5:20 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Peeling Flesh",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "4:15 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Fox Lake",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "3:20 PM",
        endTime = "",
        genre = "Nu Metal / Hardcore"
    ),
    FestivalArtist(
        artistName = "Silly Goose",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "2:25 PM",
        endTime = "",
        genre = "Nu Metal / Rap Metal"
    ),
    FestivalArtist(
        artistName = "Heavy//Hitter",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "1:30 PM",
        endTime = "",
        genre = "Deathcore"
    ),
    FestivalArtist(
        artistName = "Freeze The Fall (Twitch winner)",
        stage = "Loudmouth Stage",
        day = "Saturday, September 19",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Blood For Blood",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "7:05 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Agnostic Front",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "5:55 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Madball",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "4:50 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "H2O",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "3:50 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "The Barbarians Of California",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "2:50 PM",
        endTime = "",
        genre = "Hardcore Punk / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Many Eyes",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "1:55 PM",
        endTime = "",
        genre = "Hardcore / Metalcore"
    ),
    FestivalArtist(
        artistName = "Codefendants",
        stage = "Impact Stage",
        day = "Saturday, September 19",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Punk / Hip-Hop"
    ),
    FestivalArtist(
        artistName = "Powerman 5000",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "8:05 PM",
        endTime = "",
        genre = "Industrial Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Texas Hippie Coalition",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "7:10 PM",
        endTime = "",
        genre = "Southern Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "Tantric",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "6:10 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Otherwise",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "5:15 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Spoken",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "4:20 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "No Resolve",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "3:25 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Doobie",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "2:35 PM",
        endTime = "",
        genre = "Hip-Hop / Rap"
    ),
    FestivalArtist(
        artistName = "As You Were",
        stage = "Big Bourbon Bar",
        day = "Saturday, September 19",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Rock"
    ),
    FestivalArtist(
        artistName = "Tool",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "9:25 PM",
        endTime = "",
        genre = "Progressive Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Danny Elfman",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "7:10 PM",
        endTime = "",
        genre = "Rock / Orchestral / Experimental"
    ),
    FestivalArtist(
        artistName = "The Mars Volta",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "5:25 PM",
        endTime = "",
        genre = "Progressive Rock / Experimental Rock"
    ),
    FestivalArtist(
        artistName = "Ministry",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "3:45 PM",
        endTime = "",
        genre = "Industrial Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Filter",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "2:15 PM",
        endTime = "",
        genre = "Industrial Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Toadies",
        stage = "Louder Stage",
        day = "Sunday, September 20",
        startTime = "1:00 PM",
        endTime = "",
        genre = "Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Gojira",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "8:15 PM",
        endTime = "",
        genre = "Progressive Metal / Groove Metal"
    ),
    FestivalArtist(
        artistName = "Mastodon",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Progressive Metal / Sludge Metal"
    ),
    FestivalArtist(
        artistName = "Black Label Society",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "4:35 PM",
        endTime = "",
        genre = "Heavy Metal"
    ),
    FestivalArtist(
        artistName = "Dethklok",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "2:55 PM",
        endTime = "",
        genre = "Industrial Metal / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "Animals As Leaders",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "1:35 PM",
        endTime = "",
        genre = "Progressive Metal"
    ),
    FestivalArtist(
        artistName = "Between The Buried And Me",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "12:25 PM",
        endTime = "",
        genre = "Progressive Metal"
    ),
    FestivalArtist(
        artistName = "Rivers Of Nihil",
        stage = "Life Stage",
        day = "Sunday, September 20",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Progressive Death Metal"
    ),
    FestivalArtist(
        artistName = "The Prodigy",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "8:25 PM",
        endTime = "",
        genre = "Electronic / Big Beat"
    ),
    FestivalArtist(
        artistName = "Killswitch Engage",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "6:55 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Black Veil Brides",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Atreyu",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "4:10 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Escape The Fate",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "3:00 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "I See Stars",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "1:50 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Caskets",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Metalcore / Post-Hardcore"
    ),
    FestivalArtist(
        artistName = "Austin Carlile",
        stage = "Decibel Stage",
        day = "Sunday, September 20",
        startTime = "11:30 AM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Underoath",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "7:40 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Thrice",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "6:15 PM",
        endTime = "",
        genre = "Post-Hardcore / Alternative Rock"
    ),
    FestivalArtist(
        artistName = "Alexisonfire",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "4:50 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Haste The Day",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "3:35 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Emery",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "2:25 PM",
        endTime = "",
        genre = "Post-Hardcore / Emo"
    ),
    FestivalArtist(
        artistName = "Maylene and the Sons of Disaster",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "He Is Legend",
        stage = "Reverb Stage",
        day = "Sunday, September 20",
        startTime = "12:05 PM",
        endTime = "",
        genre = "Metalcore / Southern Rock"
    ),
    FestivalArtist(
        artistName = "Sleep Theory",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "10:05 PM",
        endTime = "",
        genre = "Hard Rock / Alternative Metal"
    ),
    FestivalArtist(
        artistName = "The Home Team",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "8:45 PM",
        endTime = "",
        genre = "Pop Rock / Alternative"
    ),
    FestivalArtist(
        artistName = "Jutes",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "7:00 PM",
        endTime = "",
        genre = "Alternative Rock / Pop"
    ),
    FestivalArtist(
        artistName = "Holywatr",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "4:50 PM",
        endTime = "",
        genre = "Alternative Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Arrows In Action",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "3:35 PM",
        endTime = "",
        genre = "Pop Rock / Alternative"
    ),
    FestivalArtist(
        artistName = "The Pretty Wild",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "2:25 PM",
        endTime = "",
        genre = "Alternative Metal / Hard Rock"
    ),
    FestivalArtist(
        artistName = "sace6",
        stage = "Loudmouth Stage",
        day = "Sunday, September 20",
        startTime = "1:15 PM",
        endTime = "",
        genre = "Rock / Metal"
    ),
    FestivalArtist(
        artistName = "Sunami",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "8:25 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "Locked Shut",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "6:55 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "End It",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "5:30 PM",
        endTime = "",
        genre = "Hardcore Punk"
    ),
    FestivalArtist(
        artistName = "200 Stab Wounds",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "4:10 PM",
        endTime = "",
        genre = "Death Metal"
    ),
    FestivalArtist(
        artistName = "Corpse Pile",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "3:00 PM",
        endTime = "",
        genre = "Death Metal"
    ),
    FestivalArtist(
        artistName = "Boltcutter",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "1:50 PM",
        endTime = "",
        genre = "Death Metal"
    ),
    FestivalArtist(
        artistName = "Surfaced",
        stage = "Impact Stage",
        day = "Sunday, September 20",
        startTime = "12:40 PM",
        endTime = "",
        genre = "Death Metal"
    ),
    FestivalArtist(
        artistName = "Future Palace",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "1:45 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Dreamwake",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "4:00 PM",
        endTime = "",
        genre = "Metalcore / Synthwave"
    ),
    FestivalArtist(
        artistName = "Resolve",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "5:25 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Downswing",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "6:20 PM",
        endTime = "",
        genre = "Metalcore / Hardcore"
    ),
    FestivalArtist(
        artistName = "Aviana",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "7:25 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "Greyhaven",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "8:20 PM",
        endTime = "",
        genre = "Metalcore"
    ),
    FestivalArtist(
        artistName = "156/Silence",
        stage = "Big Bourbon Bar",
        day = "Sunday, September 20",
        startTime = "9:30 PM",
        endTime = "",
        genre = "Metalcore"
    ),
)

val festivalLineups2026 = mapOf(
    "ARC Music Festival (IL)" to arcMusicFestival2026,
    "Riot Fest (IL)" to riotFest2026,
    "EDC Orlando (FL)" to edcOrlando2026,
    "Austin City Limits (TX)" to austinCityLimits2026,
    "Life is Beautiful (NV)" to lifeIsBeautiful2026,
    "Lost Lands (OH)" to lostLands2026,
    "Burning Man (NV)" to burningMan2026,
    "Dancefestopia (KS)" to dancefestopia2026,
    "Aftershock (CA)" to aftershock2026,
    "Electric Zoo (NY)" to electricZoo2026,
    "Louder Than Life (KY)" to louderThanLife2026,
)

class MainActivity : FragmentActivity() {

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
        MapboxOptions.accessToken = BuildConfig.MAPBOX_TOKEN

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

    override fun onUserInteraction() {
        super.onUserInteraction()
        mapViewModel.resetInactivityTimer()
    }

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

    fun cacheFestivalMapLocally(festivalName: String) {
        val point = upcomingFestivals.find { it.name == festivalName }?.center ?: return

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

    fun authenticateWithBiometrics(onSuccess: () -> Unit, onFail: () -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFail()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("StageKeeper Secure Unlock")
            .setSubtitle("Use your fingerprint, face, or screen lock.")
            .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun StageKeeperAppNavigation(viewModel: MapViewModel) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)

    val currentUser by viewModel.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    var previousScreen by remember { mutableStateOf(AppScreen.Setup) }

    val sessionExpired by viewModel.sessionExpired.collectAsState()

    LaunchedEffect(sessionExpired) {
        if (sessionExpired && currentScreen != AppScreen.Login && currentScreen != AppScreen.Splash && currentScreen != AppScreen.GoogleSignUp && currentScreen != AppScreen.Locked) {
            previousScreen = currentScreen
            currentScreen = AppScreen.Locked
        }
    }

    var userParty by remember { mutableStateOf("Select Party") }
    var userFestival by remember { mutableStateOf("Select Festival") }

    val bookmarkKey = "bookmarked_sets_${currentUser?.userId ?: "guest"}"
    var globalBookmarkedSets by remember(bookmarkKey) {
        mutableStateOf(
            sharedPrefs.getStringSet(bookmarkKey, emptySet())?.toSet() ?: emptySet()
        )
    }

    val availableParties by viewModel.availableParties.collectAsState()

    when (currentScreen) {
        AppScreen.Splash -> SplashScreen(
            viewModel = viewModel,
            onSplashComplete = { isLoggedIn, missingProfile ->
                if (isLoggedIn) {
                    if (missingProfile) {
                        currentScreen = AppScreen.GoogleSignUp
                    } else {
                        previousScreen = AppScreen.Setup
                        currentScreen = AppScreen.Locked
                    }
                } else {
                    currentScreen = AppScreen.Login
                }
            }
        )

        AppScreen.Login -> LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { currentScreen = AppScreen.Setup },
            onNavigateToSignUp = { currentScreen = AppScreen.SignUp },
            onGoogleSignUpNeeded = { currentScreen = AppScreen.GoogleSignUp }
        )

        AppScreen.SignUp -> SignUpScreen(
            viewModel = viewModel,
            onSignUpSuccess = { currentScreen = AppScreen.Login },
            onBackToLogin = { currentScreen = AppScreen.Login })

        AppScreen.GoogleSignUp -> GoogleSignUpScreen(
            viewModel = viewModel,
            onProfileComplete = { currentScreen = AppScreen.Setup },
            onCancel = {
                viewModel.logoutUser()
                currentScreen = AppScreen.Login
            }
        )

        AppScreen.Locked -> LockedScreen(
            onUnlock = {
                viewModel.clearSessionExpiredFlag()
                viewModel.resetInactivityTimer()
                currentScreen = AppScreen.Setup
            },
            onLogout = {
                viewModel.logoutUser()
                viewModel.clearSessionExpiredFlag()
                currentScreen = AppScreen.Login
            }
        )

        AppScreen.Setup -> SetupScreen(
            selectedParty = userParty,
            onPartySelected = {
                userParty = it
                viewModel.startListeningToPartyPins(it)
                viewModel.turnOnOfflineMesh()
            },
            selectedFestival = userFestival, onFestivalSelected = { userFestival = it },
            availableParties = availableParties, viewModel = viewModel,
            onLaunchMap = { previousScreen = currentScreen; currentScreen = AppScreen.Map },
            onNavigateProfile = { previousScreen = currentScreen; currentScreen = AppScreen.Profile },
            onNavigateChat = { previousScreen = currentScreen; currentScreen = AppScreen.Chat },
            onNavigateLineup = { previousScreen = currentScreen; currentScreen = AppScreen.Lineup }
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
            onNavigateHome = { previousScreen = currentScreen; currentScreen = AppScreen.Setup },
            onNavigateProfile = { previousScreen = currentScreen; currentScreen = AppScreen.Profile },
            onNavigateChat = { previousScreen = currentScreen; currentScreen = AppScreen.Chat },
            onNavigateLineup = { previousScreen = currentScreen; currentScreen = AppScreen.Lineup }
        )

        AppScreen.Profile -> ProfileScreen(
            viewModel = viewModel,
            onNavigateBack = { currentScreen = previousScreen },
            onLogout = { currentScreen = AppScreen.Login }
        )

        AppScreen.Chat -> ChatScreen(
            viewModel = viewModel,
            activeParty = userParty,
            onNavigateBack = { currentScreen = previousScreen }
        )

        AppScreen.Lineup -> LineupScreen(
            activeFestival = userFestival,
            bookmarkedSets = globalBookmarkedSets,
            onBookmarkChange = { newBookmarks ->
                globalBookmarkedSets = newBookmarks
                sharedPrefs.edit().putStringSet(bookmarkKey, newBookmarks).apply()
            },
            onNavigateBack = { currentScreen = previousScreen }
        )
    }
}

// ==========================================
// SOFT-LOCK SCREEN
// ==========================================
@Composable
fun LockedScreen(
    onUnlock: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)

    val sharedPrefs = context.getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)
    var showEmergencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        (context as MainActivity).authenticateWithBiometrics(
            onSuccess = { onUnlock() },
            onFail = { }
        )
    }

    if (showEmergencyDialog) {
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = stageKeeperPurple,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("App Locked", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Verify identity to continue", color = Color.LightGray)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                (context as MainActivity).authenticateWithBiometrics(
                    onSuccess = { onUnlock() },
                    onFail = { Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show() }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)
        ) {
            Text("Unlock App", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLogout) {
            Text("Log Out Completely", color = Color.Red)
        }

        Spacer(modifier = Modifier.height(48.dp))

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

@Composable
fun SplashScreen(viewModel: MapViewModel, onSplashComplete: (isLoggedIn: Boolean, missingProfile: Boolean) -> Unit) {
    val splashBackground = Color.Black
    val stageKeeperPurple = Color(0xFFA644FF)

    LaunchedEffect(Unit) {
        delay(2500)
        viewModel.checkAuthStatus { isLoggedIn, missingProfile ->
            onSplashComplete(isLoggedIn, missingProfile)
        }
    }

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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "StageKeeper Logo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )

            Spacer(modifier = Modifier.height(48.dp))

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
    onNavigateToSignUp: () -> Unit,
    onGoogleSignUpNeeded: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)

    val sharedPrefs = context.getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)

    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    val securePrefs = EncryptedSharedPreferences.create(
        context, "secure_login_prefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(sharedPrefs.getBoolean("remember_me", false)) }

    val savedSecurePassword = securePrefs.getString("saved_secure_password", "") ?: ""
    var showEmergencyDialog by remember { mutableStateOf(false) }

    var showForgotEmailDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var recoveryInput by remember { mutableStateOf("") }

    val attemptLogin = {
        focusManager.clearFocus()
        if (email.isNotBlank() && password.isNotBlank()) {
            viewModel.authenticateUser(email, password) { user ->
                if (user != null) {
                    if (rememberMe) {
                        sharedPrefs.edit().putString("saved_email", email).putBoolean("remember_me", true).apply()
                        securePrefs.edit().putString("saved_secure_password", password).apply()
                    } else {
                        sharedPrefs.edit().remove("saved_email").putBoolean("remember_me", false).apply()
                        securePrefs.edit().remove("saved_secure_password").apply()
                    }
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val launchGoogleSignIn = {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    viewModel.authenticateWithGoogle(googleIdTokenCredential.idToken) { success, isNewUser, msg ->
                        if (success) {
                            if (isNewUser) onGoogleSignUpNeeded() else onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Firebase Error: $msg", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Google Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showEmergencyDialog) {
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
                Button(onClick = { showEmergencyDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)) { Text("Close", color = Color.White) }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    if (showForgotEmailDialog) {
        AlertDialog(
            onDismissRequest = { showForgotEmailDialog = false; recoveryInput = "" },
            title = { Text("Recover Email", color = stageKeeperPurple) },
            text = {
                Column {
                    Text("Enter your @username or Phone Number to find your account.", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it.filterNot { char -> char.isWhitespace() } },
                        label = { Text("Username or Phone", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = stageKeeperPurple,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (recoveryInput.isNotBlank()) {
                        viewModel.recoverEmail(recoveryInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) { showForgotEmailDialog = false; recoveryInput = "" }
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)) { Text("Search", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showForgotEmailDialog = false; recoveryInput = "" }) { Text("Cancel", color = stageKeeperPurple) }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false; recoveryInput = "" },
            title = { Text("Reset Password", color = stageKeeperPurple) },
            text = {
                Column {
                    Text("Enter your email address and we will send you a reset link.", color = Color.LightGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = recoveryInput,
                        onValueChange = { recoveryInput = it.filterNot { char -> char.isWhitespace() } },
                        label = { Text("Email Address", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = stageKeeperPurple,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (recoveryInput.isNotBlank()) {
                        viewModel.resetPassword(recoveryInput) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) { showForgotPasswordDialog = false; recoveryInput = "" }
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)) { Text("Send Link", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false; recoveryInput = "" }) { Text("Cancel", color = stageKeeperPurple) }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("StageKeeper", color = stageKeeperPurple, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text("Find your crew.", color = stageKeeperBlue, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.filterNot { char -> char.isWhitespace() } },
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

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = stageKeeperPurple, checkmarkColor = Color.White))
                Text("Remember Me", color = Color.LightGray, fontSize = 14.sp)
            }

            if (email.isNotBlank() && savedSecurePassword.isNotBlank()) {
                IconButton(onClick = {
                    (context as MainActivity).authenticateWithBiometrics(
                        onSuccess = {
                            viewModel.authenticateUser(email, savedSecurePassword) { user ->
                                if (user != null) onLoginSuccess()
                                else Toast.makeText(context, "Session expired, please re-type password.", Toast.LENGTH_LONG).show()
                            }
                        },
                        onFail = { Toast.makeText(context, "Biometric Auth Failed", Toast.LENGTH_SHORT).show() }
                    )
                }) { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Biometric Login", tint = stageKeeperPurple, modifier = Modifier.size(40.dp)) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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

        Button(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp).padding(end = 8.dp)
            )
            Text("Sign in with Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { showForgotEmailDialog = true }) { Text("Forgot Email?", color = stageKeeperBlue, fontSize = 12.sp) }
            TextButton(onClick = { showForgotPasswordDialog = true }) { Text("Forgot Password?", color = stageKeeperBlue, fontSize = 12.sp) }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onNavigateToSignUp) { Text("Don't have an account? Sign Up", color = Color.LightGray) }

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
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Create Account",
            color = stageKeeperPurple,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text("Join the party securely.", color = Color.LightGray, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Required Info",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.filterNot { char -> char.isWhitespace() } },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it.filterNot { char -> char.isWhitespace() } },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = stageKeeperPurple,
                unfocusedBorderColor = Color.DarkGray
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Safety & Festival Details (Optional)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        )
        Spacer(modifier = Modifier.height(12.dp))
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
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = {
                val newUser = User(
                    email = email.trim(),
                    password = password,
                    username = username.trim().lowercase().removePrefix("@"),
                    displayName = displayName.trim(),
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
                        ).show()
                        onSignUpSuccess()
                    } else {
                        Toast.makeText(context, "Error creating account.", Toast.LENGTH_SHORT).show()
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
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignUpScreen(viewModel: MapViewModel, onProfileComplete: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)

    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf(firebaseUser?.displayName ?: "") }
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
            TextButton(onClick = onCancel) { Text("Cancel & Logout", color = Color.Red, fontWeight = FontWeight.Bold) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Google Setup", color = stageKeeperPurple, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Pick a username to finish.", color = Color.LightGray, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it.filterNot { char -> char.isWhitespace() } },
            label = { Text("Username (Required)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = displayName, onValueChange = { displayName = it }, label = { Text("Display Name (Required)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Safety & Festival Details (Optional)", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = emergencyContact, onValueChange = { emergencyContact = it }, label = { Text("Emergency Contact Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = medicalInfo, onValueChange = { medicalInfo = it }, label = { Text("Medical Info") }, modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray)
        )
        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                viewModel.completeGoogleProfile(username, displayName, phone, emergencyContact, medicalInfo) { success, msg ->
                    if (success) {
                        Toast.makeText(context, "Account Ready!", Toast.LENGTH_SHORT).show()
                        onProfileComplete()
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple), shape = RoundedCornerShape(8.dp),
            enabled = username.isNotBlank() && displayName.isNotBlank()
        ) {
            Text("Complete Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    availableParties: List<PartyGroup>,
    viewModel: MapViewModel,
    onLaunchMap: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateChat: () -> Unit,
    onNavigateLineup: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)
    var partyExpanded by remember { mutableStateOf(false) }
    var festivalExpanded by remember { mutableStateOf(false) }
    var showCreatePartyDialog by remember { mutableStateOf(false) }
    var newPartyName by remember { mutableStateOf("") }
    var showJoinPartyDialog by remember { mutableStateOf(false) }
    var joinInviteCode by remember { mutableStateOf("") }

    var showFriendsDashboard by remember { mutableStateOf(false) }
    var friendSearchQuery by remember { mutableStateOf("") }
    var showInviteFriendsDialog by remember { mutableStateOf(false) }

    val incomingInvites by viewModel.incomingInvites.collectAsState()

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
        val suggestedFriends by viewModel.suggestedFriends.collectAsState()

        Dialog(onDismissRequest = { showFriendsDashboard = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1A1A1A),
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Friends Dashboard", color = stageKeeperPurple, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

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
                                ).show()
                                onPartySelected(newPartyName)
                                showCreatePartyDialog = false
                                newPartyName = ""
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
                                    ).show()
                                    onPartySelected(resultMessage)
                                    showJoinPartyDialog = false
                                    joinInviteCode = ""
                                } else {
                                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
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
                Text("Friends", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
            Row {
                TextButton(
                    onClick = onNavigateLineup,
                    enabled = selectedFestival != "Select Festival"
                ) {
                    Text(
                        "Lineup",
                        color = if (selectedFestival != "Select Festival") stageKeeperPurple else Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }
                TextButton(onClick = onNavigateChat) {
                    Text("Chat", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onNavigateProfile) {
                    Text("Profile", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "StageKeeper",
            color = stageKeeperPurple,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Setup Your Event",
            color = Color.White,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(48.dp))

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
                onDismissRequest = { partyExpanded = false },
                modifier = Modifier.background(Color(0xFF1A1A1A))
            ) {
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
                        text = { Text(party.partyName, color = Color.White) },
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

                    TextButton(
                        onClick = { showInviteFriendsDialog = true },
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Invite", color = stageKeeperPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                }
                TextButton(onClick = {
                    viewModel.leaveParty(selectedParty) { success ->
                        if (success) {
                            onPartySelected("Select Festival")
                        }
                    }
                }) { Text("Leave Crew", color = Color.Red, fontSize = 14.sp) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                onDismissRequest = { festivalExpanded = false },
                modifier = Modifier.background(Color(0xFF1A1A1A))
            ) {
                upcomingFestivals.forEach { festivalData ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(festivalData.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(festivalData.dates, color = stageKeeperBlue, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            onFestivalSelected(festivalData.name)
                            festivalExpanded = false
                            (context as MainActivity).cacheFestivalMapLocally(festivalData.name)
                            Toast.makeText(
                                context,
                                "Caching map for ${festivalData.name}...",
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
    onNavigateProfile: () -> Unit,
    onNavigateChat: () -> Unit,
    onNavigateLineup: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val locations by viewModel.allLocations.collectAsState()
    val isLowPowerMode by viewModel.isLowPowerMode.collectAsState()

    val activePartyId = availableParties.find { it.partyName == activeParty }?.partyId ?: ""
    val activePartyLocations = locations.filter { it.partyId == activePartyId }

    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)
    val stageKeeperDark = Color(0xFF050505)

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val redDotBitmap = remember { createSimpleRedDot() }

    var currentRenderedFestival by remember { mutableStateOf("") }
    var currentOverlayState by remember { mutableStateOf(true) }

    var overlayOpacity by remember { mutableFloatStateOf(0.65f) }
    var currentRenderedOpacity by remember { mutableFloatStateOf(0.65f) }

    var showNoteDialog by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var longPressLocation by remember { mutableStateOf<Point?>(null) }

    var partyExpanded by remember { mutableStateOf(false) }
    var festivalExpanded by remember { mutableStateOf(false) }
    var showCreatePartyDialog by remember { mutableStateOf(false) }
    var newPartyName by remember { mutableStateOf("") }
    var showJoinPartyDialog by remember { mutableStateOf(false) }
    var joinInviteCode by remember { mutableStateOf("") }

    var showMapOverlay by remember { mutableStateOf(true) }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = {
                showNoteDialog = false
                longPressLocation = null
                currentNoteText = ""
            },
            title = { Text("Add a Note") },
            text = {
                Column {
                    Text(
                        "Tip: Long-press anywhere on the map to drop a custom pin at that location, or use the bottom button to drop a pin right where you are.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = currentNoteText,
                        onValueChange = { currentNoteText = it },
                        label = { Text("e.g., Meetup spot / Main Stage") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showNoteDialog = false
                    if (longPressLocation != null) {
                        viewModel.saveLocationToDatabase(
                            lat = longPressLocation!!.latitude(),
                            lng = longPressLocation!!.longitude(),
                            note = currentNoteText,
                            activePartyName = activeParty
                        )
                        Toast.makeText(context, "Custom Pin Dropped!", Toast.LENGTH_SHORT).show()
                        longPressLocation = null
                    } else {
                        (context as MainActivity).grabHardwareLocationAndSave(
                            currentNoteText,
                            activeParty
                        )
                    }
                    currentNoteText = ""
                }, colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple)) {
                    Text("Save Pin")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNoteDialog = false
                    longPressLocation = null
                    currentNoteText = ""
                }) {
                    Text("Cancel", color = stageKeeperPurple)
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
                                ).show()
                                onPartyChange(newPartyName)
                                showCreatePartyDialog = false
                                newPartyName = ""
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
                                    ).show()
                                    onPartyChange(resultMessage)
                                    showJoinPartyDialog = false
                                    joinInviteCode = ""
                                } else {
                                    Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
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

    Column(modifier = Modifier.fillMaxSize().background(stageKeeperDark)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(stageKeeperDark)
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onNavigateHome) { Text("Home", color = Color(0xFFB388FF), fontSize = 13.sp) }
                    TextButton(onClick = onNavigateChat) { Text("Chat", color = stageKeeperPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                }

                Text("StageKeeper", color = stageKeeperPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onNavigateLineup,
                        enabled = activeFestival != "Select Festival"
                    ) {
                        Text(
                            "Lineup",
                            color = if (activeFestival != "Select Festival") stageKeeperPurple else Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    TextButton(onClick = onNavigateProfile) { Text("Profile", color = stageKeeperPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

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
                            label = { Text("Party", color = Color(0xFFDDDDDD), fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                            modifier = Modifier.menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedBorderColor = stageKeeperPurple,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color(0xFFEEEEEE),
                                unfocusedTextColor = Color(0xFFEEEEEE)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = partyExpanded,
                            onDismissRequest = { partyExpanded = false },
                            modifier = Modifier.background(Color(0xFF1A1A1A))
                        ) {
                            DropdownMenuItem(text = {
                                Text("🔗 Join Crew with Code", color = stageKeeperBlue, fontWeight = FontWeight.Bold)
                            }, onClick = { partyExpanded = false; showJoinPartyDialog = true })
                            DropdownMenuItem(text = {
                                Text("➕ Create New Crew", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
                            }, onClick = { partyExpanded = false; showCreatePartyDialog = true })
                            availableParties.forEach { party ->
                                DropdownMenuItem(
                                    text = { Text(party.partyName, color = Color(0xFFEEEEEE)) },
                                    onClick = {
                                        onPartyChange(party.partyName)
                                        partyExpanded = false
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
                                    color = Color(0xFFB0E0E6),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                                )
                                TextButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activePartyObj.inviteCode))
                                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(24.dp).padding(start = 8.dp)
                                ) { Text("Copy", color = Color(0xFFCCCCCC), fontSize = 11.sp) }
                            }
                            TextButton(
                                onClick = {
                                    viewModel.leaveParty(activeParty) { success ->
                                        if (success) onPartyChange("Select Party")
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) { Text("Leave", color = Color(0xFFFF6B6B), fontSize = 11.sp) }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = festivalExpanded,
                    onExpandedChange = { festivalExpanded = !festivalExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = activeFestival,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Festival", color = Color(0xFFDDDDDD), fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = festivalExpanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedContainerColor = Color(0xFF1A1A1A),
                            unfocusedContainerColor = Color(0xFF1A1A1A),
                            focusedBorderColor = stageKeeperPurple,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color(0xFFEEEEEE),
                            unfocusedTextColor = Color(0xFFEEEEEE)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = festivalExpanded,
                        onDismissRequest = { festivalExpanded = false },
                        modifier = Modifier.background(Color(0xFF1A1A1A))
                    ) {
                        upcomingFestivals.forEach { festivalData ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(festivalData.name, color = Color(0xFFEEEEEE), fontWeight = FontWeight.Bold)
                                        Text(festivalData.dates, color = Color(0xFFB0E0E6), fontSize = 11.sp)
                                    }
                                },
                                onClick = { onFestivalChange(festivalData.name); festivalExpanded = false })
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    val themedContext = ContextThemeWrapper(ctx, androidx.appcompat.R.style.Theme_AppCompat_DayNight)
                    MapView(themedContext).apply {
                        compass.enabled = false
                        logo.enabled = false
                        attribution.enabled = false
                        mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style -> style.addImage("red_dot", redDotBitmap) }
                        annotationManager = annotations.createPointAnnotationManager()

                        gestures.addOnMapLongClickListener { point ->
                            longPressLocation = point
                            showNoteDialog = true
                            true
                        }
                    }
                },
                update = { view ->
                    val activeFest = activeFestival

                    val overlayVisible = showMapOverlay && !isLowPowerMode
                    val currentLocs = activePartyLocations

                    val festivalChanged = currentRenderedFestival != activeFest
                    val overlayChanged = currentOverlayState != overlayVisible
                    val opacityChanged = currentRenderedOpacity != overlayOpacity

                    if (festivalChanged || overlayChanged) {
                        currentRenderedFestival = activeFest
                        currentOverlayState = overlayVisible
                        currentRenderedOpacity = overlayOpacity

                        if (activeFest == "Select Festival") {
                            if (festivalChanged) {
                                view.location.enabled = true
                                view.viewport.transitionTo(
                                    view.viewport.makeFollowPuckViewportState(
                                        FollowPuckViewportStateOptions.Builder().zoom(16.0).build()
                                    )
                                )
                            }
                            view.mapboxMap.getStyle { mapStyle ->
                                if (mapStyle.styleLayerExists("festival-overlay-layer")) mapStyle.removeStyleLayer("festival-overlay-layer")
                                if (mapStyle.styleSourceExists("festival-overlay-source")) mapStyle.removeStyleSource("festival-overlay-source")
                            }
                        } else {
                            view.location.enabled = true
                            view.viewport.idle()

                            val festivalObj = upcomingFestivals.find { it.name == activeFest }

                            if (festivalChanged) {
                                festivalObj?.let { fest ->
                                    view.mapboxMap.setCamera(
                                        CameraOptions.Builder()
                                            .center(fest.center)
                                            .zoom(fest.defaultZoom)
                                            .build()
                                    )
                                }
                            }

                            view.mapboxMap.getStyle { mapStyle ->
                                if (mapStyle.styleLayerExists("festival-overlay-layer")) mapStyle.removeStyleLayer("festival-overlay-layer")
                                if (mapStyle.styleSourceExists("festival-overlay-source")) mapStyle.removeStyleSource("festival-overlay-source")

                                if (overlayVisible && festivalObj != null) {
                                    val resId = context.resources.getIdentifier(festivalObj.imageName, "drawable", context.packageName)

                                    if (resId != 0) {
                                        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, resId)
                                        val tempFile = File(context.cacheDir, "${festivalObj.imageName}.jpg")

                                        if (!tempFile.exists() && bitmap != null) {
                                            FileOutputStream(tempFile).use { out ->
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                            }
                                        }

                                        mapStyle.addSource(
                                            imageSource("festival-overlay-source") {
                                                coordinates(festivalObj.imageCoordinates)
                                                url("file://${tempFile.absolutePath}")
                                            }
                                        )

                                        val rasterLayerExt = rasterLayer("festival-overlay-layer", "festival-overlay-source") {
                                            rasterOpacity(overlayOpacity.toDouble())
                                        }

                                        if (mapStyle.styleLayerExists("waterway-label")) {
                                            mapStyle.addLayerBelow(rasterLayerExt, "waterway-label")
                                        } else {
                                            mapStyle.addLayer(rasterLayerExt)
                                        }
                                    } else {
                                        Toast.makeText(context, "Could not find image for $activeFest", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    } else if (opacityChanged) {
                        currentRenderedOpacity = overlayOpacity
                        view.mapboxMap.getStyle { style ->
                            val layer = style.getLayerAs<RasterLayer>("festival-overlay-layer")
                            layer?.rasterOpacity(overlayOpacity.toDouble())
                        }
                    }

                    annotationManager?.let { manager ->
                        manager.deleteAll()
                        val pinTextColor = if (overlayOpacity <= 0.05f || isLowPowerMode) AndroidColor.BLACK else AndroidColor.WHITE

                        val optionsList = currentLocs.map { loc ->
                            PointAnnotationOptions().withPoint(Point.fromLngLat(loc.longitude, loc.latitude))
                                .withIconImage("red_dot").withTextField(loc.note)
                                .withTextOffset(listOf(0.0, 1.5)).withTextColor(pinTextColor)
                        }
                        manager.create(optionsList)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // MAP OPACITY SLIDER
            if (activeFestival != "Select Festival" && showMapOverlay && !isLowPowerMode) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .width(140.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Map Opacity", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = overlayOpacity,
                        onValueChange = { overlayOpacity = it },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = stageKeeperPurple,
                            activeTrackColor = stageKeeperPurple,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.height(30.dp)
                    )
                    Text(
                        text = if (overlayOpacity >= 0.98f) "Map Isolated" else "Street Blend",
                        color = stageKeeperBlue,
                        fontSize = 10.sp
                    )
                }
            }

            // OPTION 3: FLOATING BATTERY TOGGLE
            FloatingActionButton(
                onClick = { viewModel.togglePowerMode(!isLowPowerMode) },
                containerColor = if (isLowPowerMode) Color(0xFF330000) else Color(0xCC000000),
                contentColor = if (isLowPowerMode) Color.Red else Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp)
                    .size(56.dp),
                shape = CircleShape
            ) {
                Text(if (isLowPowerMode) "🔋" else "⚡", fontSize = 24.sp)
            }
        }

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
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Drop Pin Where You Are", fontWeight = FontWeight.Bold, color = Color.White) }
            Button(
                onClick = { showClearConfirmDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Clear Pins", fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

// ==========================================
// LINEUP & SET SCHEDULE SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineupScreen(
    activeFestival: String,
    bookmarkedSets: Set<String>,
    onBookmarkChange: (Set<String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)

    val sets = festivalLineups2026[activeFestival] ?: emptyList()

    var selectedTab by remember { mutableStateOf("Full Lineup") }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("All") }
    var selectedStage by remember { mutableStateOf("All") }

    var rouletteSet by remember { mutableStateOf<FestivalArtist?>(null) }
    var selectedVibe by remember { mutableStateOf("All") }

    val days = remember(sets) { listOf("All") + sets.map { it.day }.distinct() }
    val stages = remember(sets) { listOf("All") + sets.map { it.stage }.distinct() }

    val vibes = remember(sets) {
        listOf("All") + sets.map { it.genre }.filter { it.isNotBlank() }.distinct()
    }

    val filteredSets = sets.filter { set ->
        val matchesSearch = searchQuery.isBlank() ||
                set.artistName.contains(searchQuery, ignoreCase = true) ||
                set.genre.contains(searchQuery, ignoreCase = true)
        val matchesDay = selectedDay == "All" || set.day == selectedDay
        val matchesStage = selectedStage == "All" || set.stage == selectedStage

        matchesSearch && matchesDay && matchesStage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("Back", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (activeFestival != "Select Festival") activeFestival else "Festival Lineups",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (sets.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Schedule TBA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No lineup information is available for $activeFestival at this time.\nCheck back later!", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { selectedTab = "Full Lineup" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "Full Lineup") stageKeeperPurple else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Full Lineup", color = Color.White, fontSize = 12.sp) }

                Button(
                    onClick = { selectedTab = "My Schedule" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "My Schedule") stageKeeperPurple else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("My Schedule", color = Color.White, fontSize = 12.sp) }

                Button(
                    onClick = { selectedTab = "Discover" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "Discover") stageKeeperBlue else Color.DarkGray),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Discover", color = if (selectedTab == "Discover") Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }

            when (selectedTab) {
                "Full Lineup" -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                        placeholder = { Text("Search artist or genre...", color = Color.Gray) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(days) { day ->
                            FilterChip(
                                selected = selectedDay == day, onClick = { selectedDay = day },
                                label = { Text(day, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = stageKeeperPurple, selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1A1A1A), labelColor = Color.LightGray
                                )
                            )
                        }
                    }
                    if (stages.size > 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(stages) { stage ->
                                FilterChip(
                                    selected = selectedStage == stage, onClick = { selectedStage = stage },
                                    label = { Text(stage, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = stageKeeperBlue, selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1A1A1A), labelColor = Color.LightGray
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredSets.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No sets match your filters.", color = Color.DarkGray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredSets) { set ->
                                val isBookmarked = bookmarkedSets.contains(set.artistName)
                                SetCard(set, isBookmarked, stageKeeperPurple, stageKeeperBlue) {
                                    onBookmarkChange(if (isBookmarked) bookmarkedSets - set.artistName else bookmarkedSets + set.artistName)
                                }
                            }
                        }
                    }
                }
                "My Schedule" -> {
                    val mySchedule = sets.filter { bookmarkedSets.contains(it.artistName) }
                        .sortedBy { parseTimeToMinutes(it.startTime) }
                        .groupBy { it.day }

                    if (mySchedule.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No sets bookmarked yet.", color = Color.DarkGray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            mySchedule.forEach { (day, dailySets) ->
                                item {
                                    Text(
                                        text = day, color = stageKeeperBlue, fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                    )
                                }
                                items(dailySets) { set ->
                                    SetCard(set, true, stageKeeperPurple, stageKeeperBlue) {
                                        onBookmarkChange(bookmarkedSets - set.artistName)
                                    }
                                }
                            }
                        }
                    }
                }
                "Discover" -> {
                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Festival Roulette", color = stageKeeperBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text("Find a random set you haven't saved yet.", color = Color.LightGray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            val unbookmarked = sets.filter { !bookmarkedSets.contains(it.artistName) }
                                            if (unbookmarked.isNotEmpty()) {
                                                rouletteSet = unbookmarked.random()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Spin the Wheel", color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    if (rouletteSet != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        SetCard(rouletteSet!!, false, stageKeeperPurple, stageKeeperBlue) {
                                            onBookmarkChange(bookmarkedSets + rouletteSet!!.artistName)
                                            rouletteSet = null
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            val bookmarkedGenres = sets.filter { bookmarkedSets.contains(it.artistName) }
                                .map { it.genre }
                                .filter { it.isNotBlank() }

                            val topGenre = bookmarkedGenres.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

                            if (topGenre != null) {
                                val recommendations = sets.filter {
                                    it.genre == topGenre && !bookmarkedSets.contains(it.artistName)
                                }.shuffled().take(3)

                                if (recommendations.isNotEmpty()) {
                                    Text("Because You Like $topGenre", color = stageKeeperPurple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    recommendations.forEach { rec ->
                                        SetCard(rec, false, stageKeeperPurple, stageKeeperBlue) {
                                            onBookmarkChange(bookmarkedSets + rec.artistName)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Text("Vibe Check", color = stageKeeperPurple, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(vibes) { vibe ->
                                    FilterChip(
                                        selected = selectedVibe == vibe, onClick = { selectedVibe = vibe },
                                        label = { Text(vibe, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = stageKeeperBlue, selectedLabelColor = Color.Black,
                                            containerColor = Color(0xFF1A1A1A), labelColor = Color.LightGray
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            val vibeFilteredSets = if (selectedVibe == "All") {
                                emptyList()
                            } else {
                                sets.filter { it.genre == selectedVibe && !bookmarkedSets.contains(it.artistName) }
                            }

                            if (selectedVibe != "All") {
                                if (vibeFilteredSets.isEmpty()) {
                                    Text("No unbookmarked sets found for this vibe.", color = Color.DarkGray)
                                } else {
                                    vibeFilteredSets.forEach { vSet ->
                                        SetCard(vSet, false, stageKeeperPurple, stageKeeperBlue) {
                                            onBookmarkChange(bookmarkedSets + vSet.artistName)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            } else {
                                Text("Select a vibe above to filter.", color = Color.DarkGray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetCard(
    set: FestivalArtist,
    isBookmarked: Boolean,
    stageKeeperPurple: Color,
    stageKeeperBlue: Color,
    onBookmarkToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(set.artistName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(set.stage, color = stageKeeperBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    if (set.startTime.isNotBlank() && set.endTime.isNotBlank()) {
                        Text(" • ", color = Color.DarkGray)
                        Text("${set.startTime} - ${set.endTime}", color = Color.LightGray, fontSize = 13.sp)
                    }
                }
                if (set.genre.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(set.genre, color = Color.Gray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onBookmarkToggle) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark Set",
                    tint = if (isBookmarked) stageKeeperPurple else Color.DarkGray
                )
            }
        }
    }
}

fun parseTimeToMinutes(timeStr: String): Int {
    if (timeStr.isBlank()) return 0
    try {
        val parts = timeStr.split(" ")
        val timeParts = parts[0].split(":")
        var hours = timeParts[0].toInt()
        val minutes = timeParts[1].toInt()
        val amPm = parts.getOrNull(1) ?: ""

        if (amPm.equals("PM", true) && hours != 12) hours += 12
        if (amPm.equals("AM", true) && hours == 12) hours = 0

        if (hours < 6) hours += 24

        return hours * 60 + minutes
    } catch (e: Exception) {
        return 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MapViewModel,
    activeParty: String,
    onNavigateBack: () -> Unit
) {
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)

    var selectedTab by remember { mutableStateOf("Crew") }
    var selectedFriend by remember { mutableStateOf<User?>(null) }
    var chatText by remember { mutableStateOf("") }

    val partyMessages by viewModel.partyMessages.collectAsState()
    val dmMessages by viewModel.dmMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val friendsList by viewModel.friendsList.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(selectedTab, activeParty) {
        if (selectedTab == "Crew" && activeParty != "Select Party") {
            viewModel.startListeningToPartyChat(activeParty)
        }
    }

    LaunchedEffect(selectedFriend) {
        selectedFriend?.let {
            viewModel.startListeningToDMs(it.userId)
        }
    }

    LaunchedEffect(partyMessages.size, dmMessages.size) {
        if (selectedTab == "Crew" && partyMessages.isNotEmpty()) {
            listState.animateScrollToItem(partyMessages.size - 1)
        } else if (selectedTab == "DMs" && selectedFriend != null && dmMessages.isNotEmpty()) {
            listState.animateScrollToItem(dmMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(stageKeeperDark).systemBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                if (selectedTab == "DMs" && selectedFriend != null) {
                    selectedFriend = null
                    viewModel.stopListeningToDMs()
                } else {
                    onNavigateBack()
                }
            }) {
                Text("Back", color = stageKeeperPurple, fontWeight = FontWeight.Bold)
            }

            Text(
                if (selectedTab == "Crew") activeParty else selectedFriend?.displayName ?: "Direct Messages",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        if (selectedFriend == null) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { selectedTab = "Crew" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "Crew") stageKeeperPurple else Color.DarkGray),
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) { Text("Crew Chat", color = Color.White) }

                Button(
                    onClick = { selectedTab = "DMs" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == "DMs") stageKeeperPurple else Color.DarkGray),
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) { Text("Direct Messages", color = Color.White) }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (selectedTab == "DMs" && selectedFriend == null) {
            LazyColumn(modifier = Modifier.weight(1f).padding(16.dp)) {
                items(friendsList) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { selectedFriend = friend },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = stageKeeperBlue, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(friend.displayName, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("@${friend.username}", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            val currentMessages = if (selectedTab == "Crew") partyMessages else dmMessages

            LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), state = listState) {
                items(currentMessages) { msg ->
                    val isMe = msg.senderId == currentUser?.userId
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                            if (!isMe && selectedTab == "Crew") {
                                Text(msg.senderName, color = Color.LightGray, fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp, start = 4.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isMe) stageKeeperPurple else Color(0xFF333333))
                                    .padding(12.dp)
                            ) {
                                Text(msg.text, color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = chatText,
                    onValueChange = { chatText = it },
                    placeholder = { Text("Message...", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = stageKeeperPurple, unfocusedBorderColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (chatText.isNotBlank()) {
                            if (selectedTab == "Crew") viewModel.sendPartyMessage(activeParty, chatText)
                            else selectedFriend?.let { viewModel.sendDirectMessage(it.userId, chatText) }
                            chatText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = stageKeeperPurple),
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("➤", fontSize = 18.sp, color = Color.White)
                }
            }
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
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone. All your data, friends, and settings will be permanently destroyed.", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount { success ->
                            if (success) {
                                Toast.makeText(context, "Account Deleted.", Toast.LENGTH_SHORT).show()
                                onLogout()
                            } else {
                                Toast.makeText(context, "Error deleting account. Try logging in again.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete Permanently", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = Color.LightGray) }
            },
            containerColor = Color(0xFF1A1A1A)
        )
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

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Delete Account", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun createSimpleRedDot(): Bitmap {
    val size = 40
    val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        color = AndroidColor.RED; style = Paint.Style.FILL; isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
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