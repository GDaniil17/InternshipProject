package com.example.myapplication.ui.today

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTodayBinding
import com.example.myapplication.ui.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_today.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val API = "acc242807675465120368b3b20bb81d1"

class TodayFragment : Fragment() {

    private lateinit var dashboardViewModel: TodayViewModel
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
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val todayViewModel by activityViewModels<TodayViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)

        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        val root: View = binding.root
        currentTemp = root.currentTemp
        minTemp = root.minTemp
        maxTemp = root.maxTemp
        descriptionView = root.description
        day = root.day
        img = root.weather_img_today
        progressBar = root.progressbar_today
        infoLayout = root.info_layout
        city = root.city

        if (todayViewModel.getTodayWeather().value != null) {
            mainHandler.post {
                infoLayout?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
            todayViewModel.getTodayWeather().value?.let { showWeatherData(it) }
        } else {
            todayViewModel.getTodayWeather().observeForever {
                todayViewModel.getTodayWeather().value?.let { showWeatherData(it) }
            }
            mainActivityViewModel.getLonLat().observeForever {
                if (todayViewModel.getTodayWeather().value == null) {
                    WeatherTask().execute()
                }
            }
        }
        return root
    }

    inner class WeatherTask(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            mainHandler.post {
                infoLayout?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
            }
            Log.d("MAIN", "https://api.openweathermap.org/data/2.5/weather?lat=" +
                    "${mainActivityViewModel.getLonLat().value?.second}&lon=${mainActivityViewModel.getLonLat().value?.first}" +
                        "&units=metric&appid=${API}&lang=ru")
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?lat=" +
                        "${mainActivityViewModel.getLonLat().value?.second}&lon=${mainActivityViewModel.getLonLat().value?.first}" +
                            "&units=metric&appid=${API}&lang=ru")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception){
                Log.d("MAIN", "doInBackground "+e.message.toString())
                null
            }
            return response
        }

        @SuppressLint("SimpleDateFormat")
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let {
                try {
                    mainHandler.post {
                        progressBar?.visibility = View.GONE
                        infoLayout?.visibility = View.VISIBLE
                    }
                    val jsonObj = JSONObject(result)
                    val main = jsonObj.getJSONObject("main")
                    val city = jsonObj.getString("name")
                    val coord = jsonObj.getJSONObject("coord")
                    val lon = coord.getString("lon")
                    val lat = coord.getString("lat")
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                    val description = weather.getString("description")
                    val temp = "Now: "+main.getString("temp")+"°С"
                    val tempMin = "Min: "+main.getString("temp_min")+"°С"
                    val tempMax = "Max: "+main.getString("temp_max")+"°С"
                    val pictureLink = "http://openweathermap.org/img/wn/${weather.getString("icon")}@2x.png"
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val netDate = Date(jsonObj.getString("dt").toLong() * 1000)
                    todayViewModel.setTodayWeather(CurrentWeatherData(sdf.format(netDate),
                        temp, tempMin, tempMax, description, pictureLink, lon,lat, city))
                    todayViewModel.getTodayWeather().value?.let { showWeatherData(it) }
                } catch (e: Exception) {
                    Log.d("MAIN", "onPostExecute "+e.message.toString())
                }
            }
        }
    }

    fun showWeatherData(newWeatherData: CurrentWeatherData) {
        descriptionView?.text = newWeatherData.description
        currentTemp?.text = newWeatherData.temp
        minTemp?.text = newWeatherData.min
        maxTemp?.text = newWeatherData.max
        //val root = binding.root
        //root.context
        context?.let { context ->
            img?.let { imgView ->
                Glide.with(context).load(newWeatherData.pictureLink).into(imgView)
            }
        }
        day?.text = newWeatherData.day
        city?.text = newWeatherData.city
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}