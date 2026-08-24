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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
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
import com.mapbox.maps.plugin.attribution.attribution
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
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

enum class AppScreen { Splash, Login, SignUp, Setup, Map, Profile, Chat, Lineup }

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

// MAPBOX HELPER: Assembles coordinates as TOP LEFT -> TOP RIGHT -> BOTTOM RIGHT -> BOTTOM LEFT
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
    val defaultZoom: Double = 14.5, // Control initial camera zoom per festival
    val imageName: String,
    val imageCoordinates: List<List<Double>>
)

// ACTIVE DATABASE
val upcomingFestivals = listOf(
    // ---------------------------------------------------------
    // Arc Music Festival
    // ---------------------------------------------------------
    FestivalData(
        name = "Arc Music Festival (IL)",
        dates = "Sep 4 - Sep 6",
        center = Point.fromLngLat(-87.66478, 41.88392),
        defaultZoom = 16.5,
        imageName = "arc_music2026",
        imageCoordinates = imageQuad(
            -87.66795, 41.88562, // TOP LEFT
            -87.66302, 41.88562, // TOP RIGHT
            -87.66302, 41.88136, // BOTTOM RIGHT
            -87.66795, 41.88136  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // RIOT FEST
    // Douglass Park - Chicago, Illinois
    // ---------------------------------------------------------
    FestivalData(
        name = "Riot Fest (IL)",
        dates = "Sep 18 - Sep 20",
        center = Point.fromLngLat(-87.6994, 41.8572),
        defaultZoom = 14.8,
        imageName = "riotfest2025",
        imageCoordinates = imageQuad(
            -87.70340, 41.86125, // TOP LEFT
            -87.69175, 41.86125, // TOP RIGHT
            -87.69175, 41.85530, // BOTTOM RIGHT
            -87.70340, 41.85530  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // EDC ORLANDO
    // Camping World Stadium / Tinker Field - Orlando, Florida
    // ---------------------------------------------------------
    FestivalData(
        name = "EDC Orlando (FL)",
        dates = "Nov 6 - Nov 8",
        center = Point.fromLngLat(-81.40275, 28.53902),
        defaultZoom = 15.2,
        imageName = "edcorlando2022",
        imageCoordinates = imageQuad(
            -81.40655, 28.54297, // TOP LEFT
            -81.39585, 28.54297, // TOP RIGHT
            -81.39585, 28.53218, // BOTTOM RIGHT
            -81.40655, 28.53218  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // AUSTIN CITY LIMITS
    // Zilker Park - Austin, Texas
    // ---------------------------------------------------------
    FestivalData(
        name = "Austin City Limits (TX)",
        dates = "Oct 2 - Oct 4",
        center = Point.fromLngLat(-97.76661, 30.26768),
        defaultZoom = 14.5,
        imageName = "austincitylimits2026",
        imageCoordinates = imageQuad(
            -97.77720, 30.27080, // TOP LEFT
            -97.76020, 30.27080, // TOP RIGHT
            -97.76020, 30.26338, // BOTTOM RIGHT
            -97.77720, 30.26338  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // LIFE IS BEAUTIFUL
    // Downtown Las Vegas, Nevada
    // ---------------------------------------------------------
    FestivalData(
        name = "Life is Beautiful (NV)",
        dates = "Sep 18 - Sep 20",
        center = Point.fromLngLat(-115.13656, 36.16931),
        defaultZoom = 15.5,
        imageName = "lifeisbeautiful2023",
        imageCoordinates = imageQuad(
            -115.139147, 36.173818, // TOP LEFT
            -115.131481, 36.170475, // TOP RIGHT
            -115.133510, 36.164371, // BOTTOM RIGHT
            -115.141628, 36.167792  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // LOST LANDS
    // Legend Valley - Thornville, Ohio
    // ---------------------------------------------------------
    FestivalData(
        name = "Lost Lands (OH)",
        dates = "Sep 25 - Sep 27",
        center = Point.fromLngLat(-82.41100, 39.93982),
        defaultZoom = 13.5,
        imageName = "lostlands2025",
        imageCoordinates = imageQuad(
            -82.42915, 39.94535, // TOP LEFT
            -82.40470, 39.94420, // TOP RIGHT
            -82.40555, 39.92870, // BOTTOM RIGHT
            -82.43000, 39.92985  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // BURNING MAN 2026
    // Black Rock City - Black Rock Desert, Nevada
    // ---------------------------------------------------------
    FestivalData(
        name = "Burning Man (NV)",
        dates = "Aug 30 - Sep 7",
        center = Point.fromLngLat(-119.207871, 40.783242),
        defaultZoom = 12.5,
        imageName = "burningman2026",
        imageCoordinates = imageQuad(
            -119.206674, 40.815992, // TOP LEFT
            -119.164790, 40.784056, // TOP RIGHT
            -119.209116, 40.750340, // BOTTOM RIGHT
            -119.251000, 40.782277  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // DANCEFESTOPIA 2026
    // Wildwood Outdoor Education Center - La Cygne, Kansas
    // ---------------------------------------------------------
    FestivalData(
        name = "Dancefestopia (KS)",
        dates = "Sep 7 - Sep 13",
        center = Point.fromLngLat(-94.668760, 38.400500),
        defaultZoom = 14.5,
        imageName = "dancefestopia2026",
        imageCoordinates = imageQuad(
            -94.675499, 38.404613, // TOP LEFT
            -94.663831, 38.404613, // TOP RIGHT
            -94.663831, 38.396208, // BOTTOM RIGHT
            -94.675499, 38.396208  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // AFTERSHOCK 2026
    // Discovery Park - Sacramento, California
    // ---------------------------------------------------------
    FestivalData(
        name = "Aftershock (CA)",
        dates = "Oct 8 - Oct 11",
        center = Point.fromLngLat(-121.50741, 38.60135),
        defaultZoom = 14.5,
        imageName = "aftershock2026",
        imageCoordinates = imageQuad(
            -121.51058, 38.60470, // TOP LEFT
            -121.50073, 38.60470, // TOP RIGHT
            -121.50073, 38.59800, // BOTTOM RIGHT
            -121.51058, 38.59800  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // ELECTRIC ZOO
    // Randall's Island Park - New York, New York
    // ---------------------------------------------------------
    FestivalData(
        name = "Electric Zoo (NY)",
        dates = "Sep 4 - Sep 6",
        center = Point.fromLngLat(-73.921154, 40.799337),
        defaultZoom = 15.8,
        imageName = "electriczoo2016",
        imageCoordinates = imageQuad(
            -73.922126, 40.801838, // TOP LEFT
            -73.918820, 40.799180, // TOP RIGHT
            -73.919998, 40.797350, // BOTTOM RIGHT
            -73.923672, 40.798980  // BOTTOM LEFT
        )
    ),

    // ---------------------------------------------------------
    // LOUDER THAN LIFE 2026
    // Kentucky Exposition Center - Louisville, Kentucky
    // ---------------------------------------------------------
    FestivalData(
        name = "Louder Than Life (KY)",
        dates = "Sep 24 - Sep 27",
        center = Point.fromLngLat(-85.74496, 38.19690),
        defaultZoom = 15.1,
        imageName = "louderthanlife2026",
        imageCoordinates = imageQuad(
            -85.74803, 38.20270, // TOP LEFT
            -85.73947, 38.20166, // TOP RIGHT
            -85.74189, 38.19110, // BOTTOM RIGHT
            -85.75045, 38.19214  // BOTTOM LEFT
        )
    )
)

// ==========================================
// FESTIVAL LINEUP DATA & MODELS
// ==========================================
data class FestivalSet(
    val artistName: String,
    val stage: String,
    val day: String,
    val startTime: String = "",
    val endTime: String = "",
    val genre: String = ""
)

val festivalLineupsDatabase = mapOf(
    "Dancefestopia (KS)" to listOf(
        // THURSDAY SEPT. 10
        FestivalSet("Paper Skies", "Emerald Stage", "Thursday", "6:00 PM", "7:00 PM", "Bass"),
        FestivalSet("Lumasi", "Emerald Stage", "Thursday", "7:00 PM", "8:00 PM", "Bass"),
        FestivalSet("Effin", "Emerald Stage", "Thursday", "8:00 PM", "9:00 PM", "Dubstep"),
        FestivalSet("Ray Volpe", "Emerald Stage", "Thursday", "9:00 PM", "10:00 PM", "Dubstep"),
        FestivalSet("Crankdat", "Emerald Stage", "Thursday", "10:15 PM", "11:45 PM", "Heavy Bass"),
        FestivalSet("Nmezee", "Lollipop Stage", "Thursday", "8:00 PM", "8:45 PM"),
        FestivalSet("Big Dyl B2B Dr3vd Nox", "Lollipop Stage", "Thursday", "8:45 PM", "9:30 PM"),
        FestivalSet("Mike HQ B2B Hooplah", "Lollipop Stage", "Thursday", "9:30 PM", "10:15 PM"),
        FestivalSet("Psilly B2B Star Complex", "Lollipop Stage", "Thursday", "10:15 PM", "11:00 PM"),
        FestivalSet("Phantom Operator", "Lollipop Stage", "Thursday", "11:00 PM", "12:00 AM"),
        FestivalSet("Grabbitz", "Lollipop Stage", "Thursday", "12:00 AM", "1:00 AM"),
        FestivalSet("Mad Dubz", "Lollipop Stage", "Thursday", "1:00 AM", "2:00 AM"),
        FestivalSet("Hexxa", "Lollipop Stage", "Thursday", "2:00 AM", "3:00 AM"),
        FestivalSet("Ozztin", "Lollipop Stage", "Thursday", "3:00 AM", "4:00 AM"),
        FestivalSet("Chmura", "Lollipop Stage", "Thursday", "4:00 AM", "5:00 AM"),
        FestivalSet("Austeria", "Lollipop Stage", "Thursday", "5:00 AM", "6:00 AM"),
        FestivalSet("Philthy B2B Hope Circuit", "Forest Stage", "Thursday", "1:00 PM", "1:45 PM"),
        FestivalSet("Balance B2B Mumbo", "Forest Stage", "Thursday", "1:45 PM", "2:30 PM"),
        FestivalSet("14All Fam", "Forest Stage", "Thursday", "2:30 PM", "3:15 PM"),
        FestivalSet("Sleeper B2B Thresh", "Forest Stage", "Thursday", "3:15 PM", "4:00 PM"),
        FestivalSet("Skrrt Cobain", "Forest Stage", "Thursday", "4:00 PM", "4:45 PM"),
        FestivalSet("Dreamzzz", "Forest Stage", "Thursday", "4:45 PM", "5:30 PM"),
        FestivalSet("Bvssbratt", "Forest Stage", "Thursday", "5:30 PM", "6:15 PM"),
        FestivalSet("Fractal Bloom", "Forest Stage", "Thursday", "6:15 PM", "7:00 PM"),
        FestivalSet("Unfettered", "Rekinection Stage", "Thursday", "5:30 PM", "6:30 PM"),
        FestivalSet("Ryan Richardson", "Rekinection Stage", "Thursday", "6:30 PM", "7:30 PM"),
        FestivalSet("Dayzero", "Rekinection Stage", "Thursday", "7:30 PM", "9:00 PM"),
        FestivalSet("Rüger", "Rekinection Stage", "Thursday", "11:45 PM", "1:00 AM"),

        // FRIDAY SEPT. 11
        FestivalSet("Lightcode by LSDREAM", "Emerald Stage", "Friday", "2:00 PM", "3:30 PM", "Meditation / Ambient"),
        FestivalSet("Riddim Slinger B2B Bluff Baby", "Emerald Stage", "Friday", "4:00 PM", "5:00 PM", "Riddim"),
        FestivalSet("Izzy Vadim", "Emerald Stage", "Friday", "5:00 PM", "6:00 PM", "Bass"),
        FestivalSet("Jaenga", "Emerald Stage", "Friday", "6:00 PM", "7:00 PM", "Bass"),
        FestivalSet("Wonkywilla", "Emerald Stage", "Friday", "7:00 PM", "8:00 PM", "Bass"),
        FestivalSet("Know Good", "Emerald Stage", "Friday", "8:00 PM", "9:00 PM", "Bass"),
        FestivalSet("Eazybaked", "Emerald Stage", "Friday", "9:00 PM", "10:00 PM", "Experimental Bass"),
        FestivalSet("Wreckno", "Emerald Stage", "Friday", "10:00 PM", "11:00 PM", "Bass"),
        FestivalSet("LSDREAM", "Emerald Stage", "Friday", "11:10 PM", "12:10 AM", "Bass"),
        FestivalSet("GRiZ", "Emerald Stage", "Friday", "12:20 AM", "1:35 AM", "Future Funk / Bass"),
        FestivalSet("Sharker", "Lollipop Stage", "Friday", "7:00 PM", "7:45 PM"),
        FestivalSet("Blare", "Lollipop Stage", "Friday", "7:45 PM", "8:30 PM"),
        FestivalSet("B!gmac B2B Meteorik", "Lollipop Stage", "Friday", "8:30 PM", "9:15 PM"),
        FestivalSet("Rüger B2B Darkwood", "Lollipop Stage", "Friday", "9:15 PM", "10:00 PM"),
        FestivalSet("Air Quotes B2B Itsnotimportant", "Lollipop Stage", "Friday", "10:00 PM", "10:45 PM"),
        FestivalSet("Acrylik B2B Anj.", "Lollipop Stage", "Friday", "10:45 PM", "11:30 PM"),
        FestivalSet("The Rico Suave", "Lollipop Stage", "Friday", "11:30 PM", "12:15 AM"),
        FestivalSet("Hokage B2B Slayday", "Lollipop Stage", "Friday", "12:15 AM", "1:00 AM"),
        FestivalSet("Mushroom Cloud", "Lollipop Stage", "Friday", "1:00 AM", "2:00 AM"),
        FestivalSet("Infekt", "Lollipop Stage", "Friday", "2:00 AM", "3:00 AM", "Riddim"),
        FestivalSet("Phrva", "Lollipop Stage", "Friday", "3:00 AM", "4:00 AM"),
        FestivalSet("Star Monster", "Lollipop Stage", "Friday", "4:00 AM", "5:00 AM"),
        FestivalSet("Pretty Sweet", "Lollipop Stage", "Friday", "5:00 AM", "6:00 AM"),
        FestivalSet("Megatron B2B Kxiti", "Forest Stage", "Friday", "4:00 PM", "4:45 PM"),
        FestivalSet("Elias True", "Forest Stage", "Friday", "4:45 PM", "5:30 PM"),
        FestivalSet("Haijack B2B Pjknik", "Forest Stage", "Friday", "5:30 PM", "6:15 PM"),
        FestivalSet("Bleach", "Forest Stage", "Friday", "6:15 PM", "7:00 PM"),
        FestivalSet("Dawni", "Forest Stage", "Friday", "10:00 PM", "10:45 PM"),
        FestivalSet("SCSI", "Forest Stage", "Friday", "10:45 PM", "11:30 PM"),
        FestivalSet("Sugar Drip", "Forest Stage", "Friday", "11:30 PM", "12:15 AM"),
        FestivalSet("Mark OG", "Forest Stage", "Friday", "12:15 AM", "1:00 AM"),
        FestivalSet("Grinz B2B Ginja Ninja", "Forest Stage", "Friday", "1:00 AM", "2:00 AM"),
        FestivalSet("Anti Plastic", "Rekinection Stage", "Friday", "12:30 PM", "1:45 PM"),
        FestivalSet("Manipadme", "Rekinection Stage", "Friday", "3:45 PM", "5:00 PM"),
        FestivalSet("Subrosa...", "Rekinection Stage", "Friday", "5:00 PM", "6:15 PM"),
        FestivalSet("Mermix", "Rekinection Stage", "Friday", "6:15 PM", "7:30 PM"),
        FestivalSet("Journey Jones", "Rekinection Stage", "Friday", "7:30 PM", "8:45 PM"),
        FestivalSet("D.Mic", "Rekinection Stage", "Friday", "8:45 PM", "10:00 PM"),
        FestivalSet("Subplay", "Rekinection Stage", "Friday", "10:00 PM", "11:00 PM"),
        FestivalSet("Rekinection Aerial-Fire-Dance Show", "Rekinection Stage", "Friday", "1:30 AM", "2:30 AM"),
        FestivalSet("Mycelium", "Pool Stage", "Friday", "12:30 PM", "1:15 PM"),
        FestivalSet("Litebug", "Pool Stage", "Friday", "1:15 PM", "2:00 PM"),
        FestivalSet("Mther", "Pool Stage", "Friday", "3:30 PM", "4:15 PM"),
        FestivalSet("N8VBOY", "Pool Stage", "Friday", "4:15 PM", "5:00 PM"),
        FestivalSet("Nowhere Further", "Pool Stage", "Friday", "5:00 PM", "5:45 PM"),
        FestivalSet("Kota Who?", "Pool Stage", "Friday", "5:45 PM", "6:30 PM"),

        // SATURDAY SEPT. 12
        FestivalSet("Dream & Friends", "Emerald Stage", "Saturday", "4:00 PM", "5:00 PM"),
        FestivalSet("Tynan", "Emerald Stage", "Saturday", "5:00 PM", "6:00 PM", "Bass / Trap"),
        FestivalSet("Smoakland", "Emerald Stage", "Saturday", "6:00 PM", "7:00 PM", "Bass"),
        FestivalSet("Reaper", "Emerald Stage", "Saturday", "7:00 PM", "8:00 PM", "Drum & Bass"),
        FestivalSet("Heyz", "Emerald Stage", "Saturday", "8:00 PM", "9:00 PM", "Bass"),
        FestivalSet("Layz", "Emerald Stage", "Saturday", "9:00 PM", "10:00 PM", "Heavy Dubstep"),
        FestivalSet("Alleycvt", "Emerald Stage", "Saturday", "10:00 PM", "11:00 PM", "Melodic Dubstep"),
        FestivalSet("Sullivan King", "Emerald Stage", "Saturday", "11:00 PM", "12:00 AM", "Metalstep"),
        FestivalSet("Excision", "Emerald Stage", "Saturday", "12:15 AM", "1:30 AM", "Dubstep"),
        FestivalSet("Half Moon", "Lollipop Stage", "Saturday", "7:00 PM", "7:45 PM"),
        FestivalSet("Rise B2B Bagz", "Lollipop Stage", "Saturday", "7:45 PM", "8:30 PM"),
        FestivalSet("Lektrik B2B Sheppa", "Lollipop Stage", "Saturday", "8:30 PM", "9:15 PM"),
        FestivalSet("Y'all Thought B2B Txana", "Lollipop Stage", "Saturday", "9:15 PM", "10:00 PM"),
        FestivalSet("Dirty Vacation B2B Imposter Sindrum", "Lollipop Stage", "Saturday", "10:00 PM", "10:45 PM"),
        FestivalSet("Elixa B2B King Coopa", "Lollipop Stage", "Saturday", "10:45 PM", "11:30 PM"),
        FestivalSet("Savage Habits B2B Botz & Bandz", "Lollipop Stage", "Saturday", "11:30 PM", "12:30 AM"),
        FestivalSet("Oliverse", "Lollipop Stage", "Saturday", "12:30 AM", "2:00 AM", "Dubstep"),
        FestivalSet("Kompany", "Lollipop Stage", "Saturday", "2:00 AM", "3:00 AM", "Dubstep"),
        FestivalSet("Calcium", "Lollipop Stage", "Saturday", "3:00 AM", "4:00 AM", "Dubstep"),
        FestivalSet("Mport", "Lollipop Stage", "Saturday", "4:00 AM", "5:00 AM"),
        FestivalSet("Just A Gent", "Lollipop Stage", "Saturday", "5:00 AM", "6:00 AM"),
        FestivalSet("Madnoiz B2B Slabb", "Forest Stage", "Saturday", "4:00 PM", "4:45 PM"),
        FestivalSet("Etrnl B2B Pandicorn", "Forest Stage", "Saturday", "4:45 PM", "5:30 PM"),
        FestivalSet("Panda", "Forest Stage", "Saturday", "5:30 PM", "6:15 PM"),
        FestivalSet("Hostile", "Forest Stage", "Saturday", "6:15 PM", "7:00 PM"),
        FestivalSet("Cinimod", "Forest Stage", "Saturday", "10:00 PM", "10:45 PM"),
        FestivalSet("Deluluz", "Forest Stage", "Saturday", "10:45 PM", "11:30 PM"),
        FestivalSet("Visions", "Forest Stage", "Saturday", "11:30 PM", "12:15 AM"),
        FestivalSet("Illite", "Forest Stage", "Saturday", "12:15 AM", "1:00 AM"),
        FestivalSet("Mob Boss B2B V Tach", "Forest Stage", "Saturday", "1:00 AM", "2:00 AM"),
        FestivalSet("Apacolypto", "Rekinection Stage", "Saturday", "1:15 PM", "2:30 PM"),
        FestivalSet("Proper Grammar", "Rekinection Stage", "Saturday", "2:30 PM", "3:45 PM"),
        FestivalSet("Subrosa...", "Rekinection Stage", "Saturday", "3:45 PM", "5:00 PM"),
        FestivalSet("G@lxy", "Rekinection Stage", "Saturday", "5:00 PM", "6:15 PM"),
        FestivalSet("Vincit", "Rekinection Stage", "Saturday", "6:15 PM", "7:30 PM"),
        FestivalSet("Darkwood B2B Callisto", "Rekinection Stage", "Saturday", "7:30 PM", "8:45 PM"),
        FestivalSet("Zero One", "Rekinection Stage", "Saturday", "8:45 PM", "10:00 PM"),
        FestivalSet("Buck Norris", "Rekinection Stage", "Saturday", "10:00 PM", "11:00 PM"),
        FestivalSet("Rekinection Aerial-Fire-Dance Show", "Rekinection Stage", "Saturday", "1:30 AM", "2:30 AM"),
        FestivalSet("Banditz", "Pool Stage", "Saturday", "12:30 PM", "1:15 PM"),
        FestivalSet("Rais3r", "Pool Stage", "Saturday", "1:15 PM", "2:00 PM"),
        FestivalSet("Risa", "Pool Stage", "Saturday", "2:00 PM", "2:45 PM"),
        FestivalSet("Alil", "Pool Stage", "Saturday", "2:45 PM", "3:30 PM"),
        FestivalSet("Texas Jack's House Party", "Pool Stage", "Saturday", "3:30 PM", "5:00 PM"),

        // SUNDAY SEPT. 13
        FestivalSet("Kyokee", "Emerald Stage", "Sunday", "4:00 PM", "5:00 PM"),
        FestivalSet("Steller", "Emerald Stage", "Sunday", "5:00 PM", "6:00 PM", "Bass"),
        FestivalSet("Probcause", "Emerald Stage", "Sunday", "6:00 PM", "7:00 PM"),
        FestivalSet("Jkyl & Hyde", "Emerald Stage", "Sunday", "7:00 PM", "8:00 PM", "Dubstep"),
        FestivalSet("Sippy", "Emerald Stage", "Sunday", "8:00 PM", "9:00 PM", "Dubstep"),
        FestivalSet("Ravenscoon", "Emerald Stage", "Sunday", "9:00 PM", "10:00 PM", "Bass"),
        FestivalSet("Inzo", "Emerald Stage", "Sunday", "10:05 PM", "11:05 PM", "Melodic Bass"),
        FestivalSet("Zeds Dead", "Emerald Stage", "Sunday", "11:20 PM", "12:50 AM", "Bass / Dubstep"),
        FestivalSet("Ncite", "Lollipop Stage", "Sunday", "7:00 PM", "7:45 PM"),
        FestivalSet("Fnu B2B Axe6", "Lollipop Stage", "Sunday", "7:45 PM", "8:30 PM"),
        FestivalSet("Spenny", "Lollipop Stage", "Sunday", "8:30 PM", "9:15 PM"),
        FestivalSet("Human Penguin B2B Saul Gucci", "Lollipop Stage", "Sunday", "9:15 PM", "10:00 PM"),
        FestivalSet("Scum Wubz B2B Larj", "Lollipop Stage", "Sunday", "10:00 PM", "10:45 PM"),
        FestivalSet("Blaqout", "Lollipop Stage", "Sunday", "10:45 PM", "11:30 PM"),
        FestivalSet("Riot Ten B2B Bear Grillz", "Lollipop Stage", "Sunday", "11:30 PM", "1:00 AM", "Dubstep"),
        FestivalSet("Samplifire", "Lollipop Stage", "Sunday", "1:00 AM", "2:00 AM", "Riddim"),
        FestivalSet("Usaybflow", "Lollipop Stage", "Sunday", "2:00 AM", "3:00 AM"),
        FestivalSet("Eliminate", "Lollipop Stage", "Sunday", "3:00 AM", "4:00 AM", "Bass / Trap"),
        FestivalSet("Dnbbq w/ Black Noise, Twotone, Hypnotizm, Kxk, Lütz, Malwar3, Theta Burn", "Forest Stage", "Sunday", "1:00 PM", "6:15 PM", "Drum & Bass"),
        FestivalSet("Habrin", "Forest Stage", "Sunday", "10:00 PM", "11:00 PM"),
        FestivalSet("Rissross", "Forest Stage", "Sunday", "11:00 PM", "12:00 AM"),
        FestivalSet("Yaws", "Rekinection Stage", "Sunday", "1:15 PM", "2:30 PM"),
        FestivalSet("Hellaquent", "Rekinection Stage", "Sunday", "2:30 PM", "3:45 PM"),
        FestivalSet("Orb.It", "Rekinection Stage", "Sunday", "3:45 PM", "5:00 PM"),
        FestivalSet("Just Tommy", "Rekinection Stage", "Sunday", "5:00 PM", "6:15 PM"),
        FestivalSet("Indigenous", "Rekinection Stage", "Sunday", "6:15 PM", "7:30 PM"),
        FestivalSet("Babysox", "Rekinection Stage", "Sunday", "7:30 PM", "8:45 PM"),
        FestivalSet("Lazuli", "Rekinection Stage", "Sunday", "8:45 PM", "10:00 PM"),
        FestivalSet("Rekinection Aerial-Fire-Dance Show", "Rekinection Stage", "Sunday", "12:50 AM", "1:50 AM"),
        FestivalSet("Jaywalk", "Pool Stage", "Sunday", "12:30 PM", "1:15 PM"),
        FestivalSet("User00215", "Pool Stage", "Sunday", "1:15 PM", "2:00 PM"),
        FestivalSet("Nick Niemeier", "Pool Stage", "Sunday", "2:00 PM", "2:45 PM"),
        FestivalSet("Aliza", "Pool Stage", "Sunday", "2:45 PM", "3:30 PM"),
        FestivalSet("Slvr Fox", "Pool Stage", "Sunday", "3:30 PM", "4:15 PM"),
        FestivalSet("Nofslinger", "Pool Stage", "Sunday", "4:15 PM", "5:00 PM"),
        FestivalSet("Down Two Freaks", "Pool Stage", "Sunday", "5:00 PM", "5:45 PM")
    ),
    "Arc Music Festival (IL)" to listOf(
        FestivalSet("Chase & Status", "The Grid", "Friday", "7:00 PM", "8:30 PM", "Drum & Bass"),
        FestivalSet("Sara Landry Presents Eternalism", "The Grid", "Friday", "8:45 PM", "10:00 PM", "Hard Techno"),
        FestivalSet("Cloonee", "The Grid", "Saturday", "7:00 PM", "8:25 PM", "Tech House"),
        FestivalSet("MAU P", "The Grid", "Saturday", "8:30 PM", "10:00 PM", "Tech House"),
        FestivalSet("The Blessed Madonna B2B Lil' Louis", "Area 909", "Saturday", "8:30 PM", "10:00 PM", "House"),
        FestivalSet("Michael Bibi", "The Grid", "Sunday", "7:00 PM", "8:30 PM", "Tech House"),
        FestivalSet("Green Velvet B2B Josh Baker", "The Grid", "Sunday", "8:30 PM", "10:00 PM", "House"),
        FestivalSet("Honey Dijon", "The Grid", "Sunday", "5:30 PM", "7:00 PM", "House")
    ),
    "Riot Fest (IL)" to listOf(
        FestivalSet("Twenty One Pilots", "Riot Stage", "Friday", "", "", "Alt Rock"),
        FestivalSet("Iggy Pop", "Roots Stage", "Friday", "", "", "Punk Rock"),
        FestivalSet("Rise Against", "Radical Stage", "Friday", "", "", "Punk Rock"),
        FestivalSet("Tool", "Riot Stage", "Saturday", "", "", "Prog Metal"),
        FestivalSet("Morrissey", "Roots Stage", "Saturday", "", "", "Indie Rock"),
        FestivalSet("NAS", "Radical Stage", "Saturday", "", "", "Hip Hop"),
        FestivalSet("Pierce The Veil", "Riot Stage", "Sunday", "", "", "Post-Hardcore"),
        FestivalSet("Alanis Morissette", "Roots Stage", "Sunday", "", "", "Alt Rock"),
        FestivalSet("Elvis Costello", "Radical Stage", "Sunday", "", "", "Rock")
    ),
    "EDC Orlando (FL)" to listOf(
        FestivalSet("David Guetta", "kineticFIELD", "Friday", "", "", "House"),
        FestivalSet("Martin Garrix", "kineticFIELD", "Friday", "", "", "Progressive House"),
        FestivalSet("Hardwell", "circuitGROUNDS", "Friday", "", "", "Electro House"),
        FestivalSet("Kaskade", "kineticFIELD", "Saturday", "", "", "House"),
        FestivalSet("Alesso (Sunset Set)", "kineticFIELD", "Saturday", "", "", "Progressive House"),
        FestivalSet("SLANDER", "circuitGROUNDS", "Saturday", "", "", "Melodic Bass"),
        FestivalSet("Afrojack", "kineticFIELD", "Sunday", "", "", "Electro House"),
        FestivalSet("Steve Aoki", "kineticFIELD", "Sunday", "", "", "Electro House"),
        FestivalSet("Alan Walker", "circuitGROUNDS", "Sunday", "", "", "Electro House")
    ),
    "Lost Lands (OH)" to listOf(
        FestivalSet("Excision (2 Hour Set)", "Prehistoric Stage", "Friday", "", "", "Dubstep"),
        FestivalSet("Ganja White Night", "Wompy Woods", "Friday", "", "", "Bass"),
        FestivalSet("SVDDEN DEATH", "Prehistoric Stage", "Saturday", "", "", "Heavy Dubstep"),
        FestivalSet("Subtronics", "Prehistoric Stage", "Saturday", "", "", "Dubstep"),
        FestivalSet("Zomboy", "Wompy Woods", "Saturday", "", "", "Dubstep"),
        FestivalSet("Excision B2B Space Laces", "Prehistoric Stage", "Sunday", "", "", "Dubstep"),
        FestivalSet("Adventure Club", "Wompy Woods", "Sunday", "", "", "Melodic Dubstep")
    ),
    "Austin City Limits (TX)" to listOf(
        FestivalSet("Skrillex", "American Express", "Friday", "", "", "Electronic"),
        FestivalSet("Charli XCX", "Honda Stage", "Friday", "", "", "Pop"),
        FestivalSet("RÜFÜS DU SOL", "American Express", "Saturday", "", "", "Electronic"),
        FestivalSet("Twenty One Pilots", "Honda Stage", "Saturday", "", "", "Alt Rock"),
        FestivalSet("Lorde", "American Express", "Sunday", "", "", "Pop"),
        FestivalSet("The xx", "Honda Stage", "Sunday", "", "", "Indie Pop")
    ),
    "Aftershock (CA)" to listOf(
        FestivalSet("My Chemical Romance", "Jack Daniel's Stage", "Thursday", "", "", "Rock"),
        FestivalSet("Sublime", "Shockwave Stage", "Thursday", "", "", "Ska Punk"),
        FestivalSet("Limp Bizkit", "Jack Daniel's Stage", "Friday", "", "", "Nu Metal"),
        FestivalSet("Wu-Tang Clan", "Shockwave Stage", "Friday", "", "", "Hip Hop"),
        FestivalSet("Pierce The Veil", "Jack Daniel's Stage", "Saturday", "", "", "Post-Hardcore"),
        FestivalSet("BABYMETAL", "Shockwave Stage", "Saturday", "", "", "Metal"),
        FestivalSet("TOOL", "Jack Daniel's Stage", "Sunday", "", "", "Prog Metal"),
        FestivalSet("Queens of the Stone Age", "Shockwave Stage", "Sunday", "", "", "Hard Rock")
    ),
    "Louder Than Life (KY)" to listOf(
        FestivalSet("Iron Maiden", "Space Zebra Stage", "Thursday", "", "", "Heavy Metal"),
        FestivalSet("Pantera", "Loudmouth Stage", "Thursday", "", "", "Metal"),
        FestivalSet("My Chemical Romance", "Space Zebra Stage", "Friday", "", "", "Rock"),
        FestivalSet("Pierce The Veil", "Loudmouth Stage", "Friday", "", "", "Post-Hardcore"),
        FestivalSet("Limp Bizkit", "Space Zebra Stage", "Saturday", "", "", "Nu Metal"),
        FestivalSet("Papa Roach", "Loudmouth Stage", "Saturday", "", "", "Nu Metal"),
        FestivalSet("TOOL", "Space Zebra Stage", "Sunday", "", "", "Prog Metal"),
        FestivalSet("Gojira", "Loudmouth Stage", "Sunday", "", "", "Heavy Metal")
    )
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
}

@Composable
fun StageKeeperAppNavigation(viewModel: MapViewModel) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("StageKeeperPrefs", Context.MODE_PRIVATE)

    // Grab the current user so we can tie their data to their account
    val currentUser by viewModel.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
    var previousScreen by remember { mutableStateOf(AppScreen.Setup) }

    // Keeping these globally so the map screen knows exactly what festival and party the user picked
    var userParty by remember { mutableStateOf("Select Party") }
    var userFestival by remember { mutableStateOf("Select Festival") }

    // Create a unique save key based on who is logged in
    val bookmarkKey = "bookmarked_sets_${currentUser?.userId ?: "guest"}"

    // The 'remember(bookmarkKey)' tells Compose to reload this data whenever a new user logs in
    var globalBookmarkedSets by remember(bookmarkKey) {
        mutableStateOf(
            sharedPrefs.getStringSet(bookmarkKey, emptySet())?.toSet() ?: emptySet()
        )
    }

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
                // Update the state AND save it directly to the specific user's file
                globalBookmarkedSets = newBookmarks
                sharedPrefs.edit().putStringSet(bookmarkKey, newBookmarks).apply()
            },
            onNavigateBack = { currentScreen = previousScreen }
        )
    }
}

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val splashBackground = Color.Black
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
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val stageKeeperDark = Color(0xFF050505)
    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)
    var email by remember { mutableStateOf("") }
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
        )
        Text(
            "Find your crew.",
            color = stageKeeperBlue,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(48.dp))
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
        )
        Spacer(modifier = Modifier.height(16.dp))
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
        )
        Spacer(modifier = Modifier.height(48.dp))
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
        }
        Spacer(modifier = Modifier.height(16.dp))
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

                    // Native Android Share Intent
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

    val activePartyId = availableParties.find { it.partyName == activeParty }?.partyId ?: ""
    val activePartyLocations = locations.filter { it.partyId == activePartyId }

    val stageKeeperPurple = Color(0xFFA644FF)
    val stageKeeperBlue = Color(0xFF00BFFF)
    val stageKeeperDark = Color(0xFF050505)

    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    val redDotBitmap = remember { createSimpleRedDot() }

    // State to track changes
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

    // Toggle for the map overlay
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onNavigateHome) {
                        Text("Home", color = Color(0xFFB388FF), fontWeight = FontWeight.Normal, fontSize = 13.sp)
                    }
                    TextButton(onClick = onNavigateChat) {
                        Text("Chat", color = stageKeeperPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
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
                    TextButton(onClick = onNavigateProfile) {
                        Text("Profile", color = stageKeeperPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
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
                                        if (success) {
                                            onPartyChange("Select Party")
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) { Text("Leave", color = Color(0xFFFF6B6B), fontSize = 11.sp) }
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

        // MAPBOX VIEW
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    val themedContext = ContextThemeWrapper(
                        ctx,
                        androidx.appcompat.R.style.Theme_AppCompat_DayNight
                    )
                    MapView(themedContext).apply {
                        compass.enabled = false
                        logo.enabled = false
                        attribution.enabled = false
                        mapboxMap.loadStyle(Style.MAPBOX_STREETS) { style ->
                            style.addImage("red_dot", redDotBitmap)
                        }
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
                    val overlayVisible = showMapOverlay
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
                            // Remove overlay if returning to default
                            view.mapboxMap.getStyle { mapStyle ->
                                if (mapStyle.styleLayerExists("festival-overlay-layer")) mapStyle.removeStyleLayer("festival-overlay-layer")
                                if (mapStyle.styleSourceExists("festival-overlay-source")) mapStyle.removeStyleSource("festival-overlay-source")
                            }
                        } else {
                            view.location.enabled = true
                            view.viewport.idle()

                            val festivalObj = upcomingFestivals.find { it.name == activeFest }

                            // ONLY reposition camera if the festival itself actually changed
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
                                // Remove old layers to avoid ID clashes
                                if (mapStyle.styleLayerExists("festival-overlay-layer")) mapStyle.removeStyleLayer("festival-overlay-layer")
                                if (mapStyle.styleSourceExists("festival-overlay-source")) mapStyle.removeStyleSource("festival-overlay-source")

                                // Only add the overlay if the user has the toggle turned ON
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

                                        // Apply exact imageQuad corners for georeferencing using the local file URL
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
                        // Dynamically switch text color based on opacity: Black at 0%, Light Gray above 0%
                        val pinTextColor = if (overlayOpacity <= 0.05f) AndroidColor.BLACK else AndroidColor.WHITE

                        val optionsList = currentLocs.map { loc ->
                            PointAnnotationOptions().withPoint(
                                Point.fromLngLat(
                                    loc.longitude,
                                    loc.latitude
                                )
                            ).withIconImage("red_dot").withTextField(loc.note)
                                .withTextOffset(listOf(0.0, 1.5)).withTextColor(pinTextColor)
                        }
                        manager.create(optionsList)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // FLOATING UI: Slider to isolate the map opacity
            if (activeFestival != "Select Festival" && showMapOverlay) {
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

    val sets = festivalLineupsDatabase[activeFestival] ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDay by remember { mutableStateOf("All") }
    var selectedStage by remember { mutableStateOf("All") }
    var showOnlyBookmarked by remember { mutableStateOf(false) }

    // Persistent bookmark set for favorite artists
    val days = remember(sets) { listOf("All") + sets.map { it.day }.distinct() }
    val stages = remember(sets) { listOf("All") + sets.map { it.stage }.distinct() }

    val filteredSets = sets.filter { set ->
        val matchesSearch = searchQuery.isBlank() ||
                set.artistName.contains(searchQuery, ignoreCase = true) ||
                set.genre.contains(searchQuery, ignoreCase = true)
        val matchesDay = selectedDay == "All" || set.day == selectedDay
        val matchesStage = selectedStage == "All" || set.stage == selectedStage
        val matchesBookmark = !showOnlyBookmarked || bookmarkedSets.contains(set.artistName)

        matchesSearch && matchesDay && matchesStage && matchesBookmark
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(stageKeeperDark)
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Navigation Row
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
            IconButton(onClick = { showOnlyBookmarked = !showOnlyBookmarked }) {
                Icon(
                    imageVector = if (showOnlyBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Show Bookmarked",
                    tint = if (showOnlyBookmarked) stageKeeperPurple else Color.LightGray
                )
            }
        }

        if (sets.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Schedule TBA",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No lineup information is available for $activeFestival at this time.\nCheck back later!",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray) },
                placeholder = { Text("Search artist or genre...", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = stageKeeperPurple,
                    unfocusedBorderColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Day Selector Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(days) { day ->
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(day, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = stageKeeperPurple,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1A1A1A),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            // Stage Filter Chips
            if (stages.size > 2) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stages) { stage ->
                        FilterChip(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            label = { Text(stage, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = stageKeeperBlue,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1A1A1A),
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lineup Cards List
            if (filteredSets.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sets match your filters.", color = Color.DarkGray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredSets) { set ->
                        val isBookmarked = bookmarkedSets.contains(set.artistName)
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
                                IconButton(
                                    onClick = {
                                        val updatedBookmarks = if (isBookmarked) {
                                            bookmarkedSets - set.artistName
                                        } else {
                                            bookmarkedSets + set.artistName
                                        }
                                        onBookmarkChange(updatedBookmarks)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark Set",
                                        tint = if (isBookmarked) stageKeeperPurple else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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

    // Auto-listen to crew chat when tab opens
    LaunchedEffect(selectedTab, activeParty) {
        if (selectedTab == "Crew" && activeParty != "Select Party") {
            viewModel.startListeningToPartyChat(activeParty)
        }
    }

    // Auto-listen to DM when friend selected
    LaunchedEffect(selectedFriend) {
        selectedFriend?.let {
            viewModel.startListeningToDMs(it.userId)
        }
    }

    // Auto-scroll to bottom on new message
    LaunchedEffect(partyMessages.size, dmMessages.size) {
        if (selectedTab == "Crew" && partyMessages.isNotEmpty()) {
            listState.animateScrollToItem(partyMessages.size - 1)
        } else if (selectedTab == "DMs" && selectedFriend != null && dmMessages.isNotEmpty()) {
            listState.animateScrollToItem(dmMessages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(stageKeeperDark).systemBarsPadding()) {
        // Header
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

        // Tabs
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

        // Body
        if (selectedTab == "DMs" && selectedFriend == null) {
            // Friend List
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
            // Chat Messages
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

            // Input Field
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