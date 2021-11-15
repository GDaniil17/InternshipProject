package com.example.myapplication.ui.seven_days

data class WeekWeatherData(
    val minTemp: Double,
    val maxTemp: Double,
    val morningTemp: Double,
    val dayTemp: Double,
    val eveningTemp: Double,
    val nightTemp: Double,
    val description: String,
    val day: Int,
    val img: String
)
