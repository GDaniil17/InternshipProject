package com.example.myapplication.ui.today

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
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTodayBinding
import com.example.myapplication.ui.MainActivityViewModel
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {
    //TODO add search for specific city
    //TODO add loading cycle before showing weather
    //TODO extract weathers into viewModel

    val API = "7b7ebabc7f47bb63c0d5dc37e076bc8a"

    private lateinit var dashboardViewModel: TodayViewModel
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    var currentTemp: TextView? = null
    var minTemp: TextView? = null
    var maxTemp: TextView? = null
    var descriptionView: TextView? = null
    var day: TextView? = null
    var img: ImageView? = null
    var progressBar: ProgressBar? = null
    var infoLayout: LinearLayout? = null
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val todayViewModel by activityViewModels<TodayViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)

        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        val root: View = binding.root
        currentTemp = root.findViewById(R.id.currentTemp)
        minTemp = root.findViewById(R.id.minTemp)
        maxTemp = root.findViewById(R.id.maxTemp)
        descriptionView = root.findViewById(R.id.description)
        day = root.findViewById(R.id.day)
        img = root.findViewById(R.id.weather_img_today)
        progressBar = root.findViewById(R.id.progressbar_today)
        infoLayout = root.findViewById(R.id.info_layout)

        if (todayViewModel.getTodayWeather() != null) {
            mainHandler.post {
                infoLayout?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
            todayViewModel.getTodayWeather()?.let { showWeatherData(it) }
        } else {
            mainActivityViewModel.getLonLat().observeForever {
                if (todayViewModel.getTodayWeather() == null) {
                    weatherTask().execute()
                }
            }
        }
        return root
    }

    inner class weatherTask(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            mainHandler.post {
                infoLayout?.visibility = View.GONE
                progressBar?.visibility = View.VISIBLE
            }
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
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                    val description = weather.getString("description")
                    val temp = "Now: "+main.getString("temp")+"°С"
                    val tempMin = "Min: "+main.getString("temp_min")+"°С"
                    val tempMax = "Max: "+main.getString("temp_max")+"°С"

                    val pictureLink = "http://openweathermap.org/img/wn/${weather.getString("icon")}@2x.png"
                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val netDate = Date(jsonObj.getString("dt").toLong() * 1000)
                    todayViewModel.setTodayWeather(CurrentWeatherData(sdf.format(netDate),
                        temp, tempMin, tempMax, description, pictureLink))
                    todayViewModel.getTodayWeather()?.let { showWeatherData(it) }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}