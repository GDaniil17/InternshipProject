package com.example.myapplication.ui.today.response

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)