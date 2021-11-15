package com.example.myapplication.ui.seven_days.response

import com.google.gson.annotations.SerializedName

data class WeekResponse(
    val alerts: List<Alert>,
    val current: Current,
    val daily: List<Daily>,
    val lat: Double,
    val lon: Double,
    val timezone: String,
    @SerializedName("timezone_offset")
    val timezoneOffset: Int
)