package com.example.myapplication.ui.seven_days

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentSevenDaysBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.today.API
import kotlinx.android.synthetic.main.fragment_seven_days.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.util.*

class SevenDaysFragment : Fragment() {

    private var _binding: FragmentSevenDaysBinding? = null
    private val binding get() = _binding!!
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    val job: Job = Job()
    val scope = CoroutineScope(Dispatchers.Default + job)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSevenDaysBinding.inflate(inflater, container, false)
        val root: View = binding.root
        progressBar = root.progressbar_week
        recyclerView = root.weather_recyclerView

        Log.d("MAIN", "SevenDaysFragment")
        getResponse()
        return root
    }

    fun doWork(): Deferred<String?> = scope.async {
        mainHandler.post {
            progressBar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        }
        val response = try {
            URL(
                "https://api.openweathermap.org/data/2.5/onecall?lat=${
                    mainActivityViewModel
                        .getLonLat().value?.second
                }&lon=${mainActivityViewModel.getLonLat().value?.first}" +
                        "&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru"
            )
                .readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.d("MAIN", "doInBackground " + e.message.toString())
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
                    mainHandler.post {
                        recyclerView?.visibility = View.VISIBLE
                        progressBar?.visibility = View.GONE
                    }
                    val jsonObj = JSONObject(response)
                    val daily = jsonObj.getJSONArray("daily")
                    val weatherList = mutableListOf<WeekWeatherData>()
                    for (i: Int in 1..7) {
                        daily.getJSONObject(i).getJSONObject("temp").let {
                            weatherList.add(
                                WeekWeatherData(
                                    it.getString("min").toFloat(),
                                    it.getString("max").toFloat(),
                                    it.getString("morn").toFloat(),
                                    it.getString("day").toFloat(),
                                    it.getString("eve").toFloat(),
                                    it.getString("night").toFloat(),
                                    daily.getJSONObject(i).getJSONArray("weather").getJSONObject(0)
                                        .getString("description"),
                                    daily.getJSONObject(i).getString("dt").toLong(),
                                    "http://openweathermap.org/img/wn/${
                                        daily.getJSONObject(i).getJSONArray("weather")
                                            .getJSONObject(0).getString("icon")
                                    }@2x.png"
                                )
                            )
                        }
                    }
                    recyclerView?.let {
                        it.layoutManager = LinearLayoutManager(context)
                        it.adapter = RecyclerAdapter(weatherList)
                    }
                    Log.d("MAIN", "Finished!")
                } catch (e: Exception) {
                    Log.d("MAIN", "onPostExecute " + e.message.toString())
                }
            }
        } catch (e: Exception) {
            Log.d("MAIN", e.message.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}