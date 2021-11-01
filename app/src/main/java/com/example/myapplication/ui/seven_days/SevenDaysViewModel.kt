package com.example.myapplication.ui.seven_days

import androidx.lifecycle.ViewModel

class SevenDaysViewModel : ViewModel() {
    private var weekWeather: MutableList<WeekWeatherData>? = null

    fun getWeekWeather() = weekWeather
    fun setWeekWeather(newWeatherCast: MutableList<WeekWeatherData>) {
        weekWeather = newWeatherCast
    }
}