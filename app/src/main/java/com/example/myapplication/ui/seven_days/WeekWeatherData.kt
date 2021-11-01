package com.example.myapplication.ui.seven_days

data class WeekWeatherData(
    val minTemp: Float,
    val maxTemp: Float,
    val morningTemp: Float,
    val dayTemp: Float,
    val eveningTemp: Float,
    val nightTemp: Float,
    val description: String,
    val day: Long,
    val img: String
)
