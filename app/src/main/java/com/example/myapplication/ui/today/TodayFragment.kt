package com.example.myapplication.ui.today

import android.graphics.Bitmap
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
import com.example.myapplication.databinding.FragmentTodayBinding
import kotlinx.android.synthetic.main.fragment_today.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory


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
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    val job: Job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

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
        progressBar = root.progressbar_today
        infoLayout = root.info_layout
        city = root.city

        Log.d("MAIN", "TodayFragment")
        getResponse()
        return root
    }

    fun doWork(): Deferred<String?> = scope.async {
        mainHandler.post {
            infoLayout?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
        }

        val response = try {
            URL("https://api.openweathermap.org/data/2.5/weather?lat=" +
                    "0.0&lon=0.0" +
                    "&units=metric&appid=${API}&lang=ru")
                .readText(Charsets.UTF_8)
        } catch (e: Exception){
            Log.d("MAIN", "doInBackground "+e.message.toString())
            null
        }
        return@async response
    }

    fun getResponse() = scope.launch {
        try {
            val response = doWork().await()
            Log.d("MAIN", "!!! $response")
            response?.let {
                try {
                    val jsonObj = JSONObject(response)
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

                    val currentWeatherData = CurrentWeatherData(sdf.format(netDate), temp, tempMin, tempMax,
                        description, pictureLink, lon, lat, city)
                    showWeatherData(currentWeatherData)

                    Log.d("MAIN", "Finished!")
                } catch (e: Exception) {
                    Log.d("MAIN", "onPostExecute "+e.message.toString())
                }
            }
        } catch (e: Exception) {
            Log.d("MAIN", e.message.toString())
        }
    }

    fun getImg(pictureLink: String): Deferred<Bitmap> = scope.async {
        val url = URL(pictureLink)
        return@async BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    private fun showWeatherData(newWeatherData: CurrentWeatherData) = scope.launch {
        descriptionView?.text = newWeatherData.description
        currentTemp?.text = newWeatherData.temp
        minTemp?.text = newWeatherData.min
        maxTemp?.text = newWeatherData.max
        day?.text = newWeatherData.day
        city?.text = newWeatherData.city

        val imgResponse = getImg(newWeatherData.pictureLink).await()
        mainHandler.post {
            img?.setImageBitmap(imgResponse)
            infoLayout?.visibility = View.VISIBLE
            progressBar?.visibility = View.GONE
        }

        Log.d("MAIN", "RETURN")
        return@launch
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}