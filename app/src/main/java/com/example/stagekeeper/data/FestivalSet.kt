package com.example.stagekeeper.data

data class FestivalSet(
    val artistName: String,
    val stage: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val genre: String = ""
)