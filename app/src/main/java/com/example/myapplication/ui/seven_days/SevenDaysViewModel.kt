package com.example.myapplication.ui.seven_days

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SevenDaysViewModel : ViewModel() {
    private var weekWeather = MutableLiveData<MutableList<WeekWeatherData>>()

    fun getWeekWeather() = weekWeather
    fun setWeekWeather(newWeatherCast: MutableList<WeekWeatherData>) {
        weekWeather.postValue(newWeatherCast)
    }
}