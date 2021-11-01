package com.example.myapplication.ui.today

data class CurrentWeatherData(
    val day: String,
    val temp: String,
    val min: String,
    val max: String,
    val description: String,
    val pictureLink: String,
    val longitude: String,
    val latitude: String,
    val city: String
)
