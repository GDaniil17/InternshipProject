package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    private var lonLat = MutableLiveData(Pair(37.6156, 55.7522))
    fun getLonLat() = lonLat
    fun setLonLat(longitude: Double, latitude: Double) {
        lonLat.postValue(Pair(longitude, latitude))
    }
}