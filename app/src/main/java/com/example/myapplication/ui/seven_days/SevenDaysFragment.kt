package com.example.myapplication.ui.seven_days

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSevenDaysBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.today.API
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class SevenDaysFragment : Fragment() {
    
    private val sevenDaysViewModel by viewModels<SevenDaysViewModel>()
    private var _binding: FragmentSevenDaysBinding? = null
    private val binding get() = _binding!!
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSevenDaysBinding.inflate(inflater, container, false)
        val root: View = binding.root
        progressBar = root.findViewById(R.id.progressbar_week)
        recyclerView = root.findViewById(R.id.weather_recyclerView)

        if (sevenDaysViewModel.getWeekWeather().value != null) {
            mainHandler.post {
                recyclerView?.visibility = View.VISIBLE
                progressBar?.visibility = View.GONE
            }
            recyclerView?.let {
                it.layoutManager = LinearLayoutManager(context)
                sevenDaysViewModel.getWeekWeather().value?.let { weekWeather ->
                    it.adapter = RecyclerAdapter(weekWeather)
                }
            }
        } else {
            sevenDaysViewModel.getWeekWeather().observeForever {
                recyclerView?.let {
                    it.layoutManager = LinearLayoutManager(context)
                    sevenDaysViewModel.getWeekWeather().value?.let { weekWeather ->
                        it.adapter = RecyclerAdapter(weekWeather)
                    }
                }
            }
            mainActivityViewModel.getLonLat().observeForever {
                Log.d("MAIN", mainActivityViewModel.getLonLat().toString())
                if (sevenDaysViewModel.getWeekWeather().value == null) {
                    WeatherTask().execute()
                }
            }
        }
        return root
    }

    inner class WeatherTask(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {

            mainHandler.post {
                progressBar?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
            }
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/onecall?lat=${mainActivityViewModel
                    .getLonLat().value?.second}&lon=${mainActivityViewModel.getLonLat().value?.first}" +
                        "&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru")
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
                        recyclerView?.visibility = View.VISIBLE
                        progressBar?.visibility = View.GONE
                    }
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
                                "http://openweathermap.org/img/wn/${daily.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon")}@2x.png"
                            ))
                        }
                    }
                    sevenDaysViewModel.setWeekWeather(weatherList)

                } catch (e: Exception) {
                    Log.d("MAIN", "onPostExecute "+e.message.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}