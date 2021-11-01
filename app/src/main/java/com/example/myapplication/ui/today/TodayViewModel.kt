package com.example.myapplication.ui.today

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TodayViewModel : ViewModel() {
    private var todayWeather = MutableLiveData<CurrentWeatherData>()

    fun getTodayWeather() = todayWeather
    fun setTodayWeather(newWeatherCast: CurrentWeatherData) {
        todayWeather.postValue(newWeatherCast)
    }
}