package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
        } else {
            initialize()
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                0,0f, locationListener as LocationListener)
        }
    }

    private fun initialize() {
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                mainActivityViewModel.setLonLat(location.longitude, location.latitude)
                Log.d("MAIN", "${location.latitude} ${location.longitude}")
            }
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_today, R.id.navigation_seven_days, R.id.navigation_search
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val allowed = grantResults.sum()
        if (allowed == 0) {
            initialize()
        } else if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /*
    inner class weatherTask(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            Log.d("MAIN", "doInBackground! 2")
            Log.d("MAIN", "https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&units=metric&appid=${API}&lang=ru")
            val response = null
            if (longitude != 0.0 && latitude != 0.0) {
                try {
                    var response = URL("https://api.openweathermap.org/data/2.5/weather?lat=${latitude}&lon=${longitude}&units=metric&appid=${API}&lang=ru")
                            .readText(Charsets.UTF_8)
                } catch (e: Exception){
                    Log.d("MAIN", e.message.toString())
                }
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let {
                try {
                    val root = binding.root
                    Log.d("MAIN", "OK")
                    val jsonObj = JSONObject(result)
                    val daily = jsonObj.getJSONArray("daily")
                    val weatherList = mutableListOf<WeekWeatherData>()
                    for (i: Int in 1..7) {
                        daily.getJSONObject(i).getJSONObject("temp").let {
                            weatherList.add(WeekWeatherData(
                                it.getString("min").toFloat(),
                                it.getString("max").toFloat(),
                                it.getString("morn").toFloat(),
                                it.getString("day").toFloat(),
                                it.getString("eve").toFloat(),
                                it.getString("night").toFloat(),
                                daily.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description"),
                                daily.getJSONObject(i).getString("dt").toLong(),
                                ""
                            ))
                        }
                    }
                    mainActivityViewModel.setWeather(weatherList)

                } catch (e: Exception) {
                    Log.d("MAIN", e.message.toString())
                    Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

     */
}