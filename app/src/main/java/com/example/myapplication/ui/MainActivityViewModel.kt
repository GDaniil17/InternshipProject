package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import com.example.myapplication.ui.seven_days.WeekWeatherData
import com.example.myapplication.ui.today.CurrentWeatherData

class MainActivityViewModel: ViewModel() {
    private var longitude: Double? = null
    private var latitude: Double? = null
    private var weatherCast: MutableList<WeekWeatherData>? = null
    private var todayWeather: CurrentWeatherData? = null

    fun getWeather() = weatherCast
    fun setWeather(newWeatherCast: MutableList<WeekWeatherData>) {
        weatherCast = newWeatherCast
    }

    fun getTodayWeather() = todayWeather
    fun setTodayWeather(newWeatherCast: CurrentWeatherData) {
        todayWeather = newWeatherCast
    }

    fun getLatitude() = latitude
    fun setLatitude(newLatitude: Double) {
        latitude = newLatitude
    }

    fun getLongitude() = longitude
    fun setLongitude(newLongitude: Double) {
        longitude = newLongitude
    }
}