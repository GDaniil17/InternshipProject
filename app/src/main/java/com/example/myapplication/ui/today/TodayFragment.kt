package com.example.myapplication.ui.today

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentTodayBinding
import kotlinx.android.synthetic.main.fragment_today.view.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.today.response.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson


const val API = "acc242807675465120368b3b20bb81d1"

class TodayFragment : Fragment() {
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    var currentTemp: TextView? = null
    var minTemp: TextView? = null
    var maxTemp: TextView? = null
    var descriptionView: TextView? = null
    var day: TextView? = null
    var img: ImageView? = null
    var city: TextView? = null
    var progressBar: ProgressBar? = null
    var infoLayout: LinearLayout? = null
    var locationBtn: ImageButton? = null
    private var locationManager: LocationManager? = null
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private var locationListener: LocationListener? = null
    private val job: Job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                addLocationListener()
            } else {
                locationBtn?.isClickable = true
                showMsg("Location denied")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        val root: View = binding.root
        currentTemp = root.currentTemp
        minTemp = root.minTemp
        maxTemp = root.maxTemp
        descriptionView = root.description
        day = root.day
        img = root.weather_img_today
        city = root.city
        progressBar = root.progressbar_today
        infoLayout = root.info_layout
        locationBtn = root.btn_get_location

        locationBtn?.setOnClickListener {
            locationBtn?.isClickable = false
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            addLocationListener()
        }
        getResponse()
        mainActivityViewModel.getLonLat().observeForever {
            getResponse()
        }
        Log.d("MAIN", "TodayFragment")
        return root
    }

    private fun addLocationListener() {
        context?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

                locationManager =
                    it.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
                if (locationManager == null || (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false)) {
                    showMsg("Turn on location and try again")
                    locationBtn?.isClickable = true
                } else {
                    locationListener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            mainActivityViewModel.setLonLat(location.longitude, location.latitude)
                            showMsg("The location successfully detected")
                            Log.d("MAIN", "${location.latitude} ${location.longitude}")
                            locationListener?.let { locationManager?.removeUpdates(it) }
                        }
                    }
                    locationListener?.let { listener ->
                        locationManager?.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            0,
                            0f,
                            listener
                        )
                    }
                }
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun doWorkAsync(): Deferred<String?> = scope.async {
        mainHandler.post {
            infoLayout?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
            locationBtn?.visibility = View.GONE
        }
        var lon = 37.6156
        var lat = 55.7522
        mainActivityViewModel.getLonLat().value?.let {
            lon = it.first
            lat = it.second
        }
        val response = try {
            URL("https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}" +
                    "&units=metric&appid=${API}&lang=ru")
                .readText(Charsets.UTF_8)
        } catch (e: Exception){
            showMsg("Turn on the Internet")
            Log.d("MAIN", "doInBackground "+e.message.toString())
            return@async null
        }
        return@async response
    }

    private fun getResponse() = scope.launch {
        try {
            val response = doWorkAsync().await()
            Log.d("MAIN", "!!! $response")
            response?.let {
                try {
                    val data = Gson().fromJson(response, Response::class.java)
                    val city = data.name
                    val lon = data.coord.lon.toString()
                    val lat = data.coord.lat.toString()
                    val description = data.weather[0].description
                    val temp = "Now: "+data.main.temp
                    val tempMin = "Min: "+data.main.tempMin
                    val tempMax = "Max: "+data.main.tempMax
                    val pictureLink = "http://openweathermap.org/img/wn/${data.weather[0].icon}@2x.png"
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val netDate = Date(data.dt.toLong()*1000)
                    val currentWeatherData = CurrentWeatherData(sdf.format(netDate), temp, tempMin, tempMax,
                        description, pictureLink, lon, lat, city)
                    showWeatherData(currentWeatherData)

                    Log.d("MAIN", "Finished!")
                } catch (e: Exception) {
                    showMsg("Something went wrong")
                    Log.d("MAIN", "onPostExecute "+e.message.toString())
                }
            }
        } catch (e: Exception) {
            showMsg("Something went wrong")
            Log.d("MAIN", e.message.toString())
        }
    }

    private fun getImg(pictureLink: String): Deferred<Bitmap> = scope.async {
        val url = URL(pictureLink)
        return@async BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    private fun showWeatherData(newWeatherData: CurrentWeatherData) = scope.launch {
        val imgResponse = getImg(newWeatherData.pictureLink).await()
        mainHandler.post {
            descriptionView?.text = newWeatherData.description
            currentTemp?.text = newWeatherData.temp
            minTemp?.text = newWeatherData.min
            maxTemp?.text = newWeatherData.max
            day?.text = newWeatherData.day
            city?.text = newWeatherData.city
            img?.setImageBitmap(imgResponse)
            infoLayout?.visibility = View.VISIBLE
            progressBar?.visibility = View.GONE
            locationBtn?.visibility = View.VISIBLE
        }
        Log.d("MAIN", "RETURN")
        return@launch
    }

    private fun showMsg(text: String) {
        context?.let {
            mainHandler.post {
                Toast.makeText(it, text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}