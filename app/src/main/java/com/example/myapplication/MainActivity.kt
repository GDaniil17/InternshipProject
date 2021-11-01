package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
            locationListener?.let { listener ->
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0f, listener as LocationListener)
            }
        }
    }

    private fun initialize() {
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager == null || (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false
                    || locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == false)) {
            Log.d("MAIN", "Turn on location and restart the app")

            Toast.makeText(applicationContext, "Turn on location and restart the app", Toast.LENGTH_LONG).show()
        } else {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (mainActivityViewModel.getLonLat().value == null) {
                        mainActivityViewModel.setLonLat(location.longitude, location.latitude)
                        Log.d("MAIN", "${location.latitude} ${location.longitude}")
                    }
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
}