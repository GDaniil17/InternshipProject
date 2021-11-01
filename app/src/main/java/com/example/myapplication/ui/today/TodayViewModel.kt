package com.example.myapplication.ui.today

import androidx.lifecycle.ViewModel

class TodayViewModel : ViewModel() {
    private var todayWeather: CurrentWeatherData? = null

    fun getTodayWeather() = todayWeather
    fun setTodayWeather(newWeatherCast: CurrentWeatherData) {
        todayWeather = newWeatherCast
    }
}