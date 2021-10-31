package com.example.myapplication.ui.today

data class CurrentWeatherData(
    val day: Long,
    val temp: Float,
    val min: Float,
    val max: Float,
    val description: String
)
