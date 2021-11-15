package com.example.myapplication.ui.seven_days

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSevenDaysBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.seven_days.response.WeekResponse
import com.example.myapplication.ui.today.API
import com.example.myapplication.ui.today.response.Response
import com.google.gson.Gson
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
    private val job: Job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

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

    private fun doWorkAsync(): Deferred<String?> = scope.async {
        mainHandler.post {
            progressBar?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        }
        var lon = 37.6156
        var lat = 55.7522
        mainActivityViewModel.getLonLat().value?.let {
            lon = it.first
            lat = it.second
        }
        val response = try {
            URL(
                "https://api.openweathermap.org/data/2.5/onecall?lat=${lat}&lon=${lon}" +
                        "&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru"
            )
                .readText(Charsets.UTF_8)
        } catch (e: Exception) {
            showMsg("Turn on the Internet")
            Log.d("MAIN", "doInBackground " + e.message.toString())
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
                    mainHandler.post {
                        recyclerView?.visibility = View.VISIBLE
                        progressBar?.visibility = View.GONE
                    }
                    val weatherList = mutableListOf<WeekWeatherData>()
                    val data = Gson().fromJson(response, WeekResponse::class.java)
                    Log.d("MAIN", "${data}!")
                    for (i: Int in 1..7) {
                        data.daily[i].temp.apply {
                            weatherList.add(WeekWeatherData(
                                min,
                                max,
                                morn,
                                day,
                                eve,
                                night,
                                data.daily[i].weather[0].description,
                                data.daily[i].dt,
                                "http://openweathermap.org/img/wn/${data.daily[i].weather[0].icon}@2x.png"))
                        }
                    }

                    recyclerView?.let {
                        it.layoutManager = LinearLayoutManager(context)
                        it.adapter = RecyclerAdapter(weatherList)
                    }
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