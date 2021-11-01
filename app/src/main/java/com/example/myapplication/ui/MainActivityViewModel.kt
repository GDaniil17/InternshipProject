package com.example.myapplication.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    private var lonLat = MutableLiveData<Pair<Double,Double>>()

    fun getLonLat() = lonLat
    fun setLonLat(longitude: Double, latitude: Double) {
        lonLat.postValue(Pair(longitude, latitude))
    }
}