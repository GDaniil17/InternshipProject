package com.example.myapplication.ui.seven_days

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSevenDaysBinding
import com.example.myapplication.ui.MainActivityViewModel
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class SevenDaysFragment : Fragment() {

    val API = "afa2b1809a6d5d5aa9ea2f64420da228"

    private lateinit var sevenDaysViewModel: SevenDaysViewModel
    private var _binding: FragmentSevenDaysBinding? = null
    private val binding get() = _binding!!
    private var recyclerView: RecyclerView? = null
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    //private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sevenDaysViewModel =
            ViewModelProvider(this).get(SevenDaysViewModel::class.java)

        _binding = FragmentSevenDaysBinding.inflate(inflater, container, false)
        val root: View = binding.root
        recyclerView = root.findViewById(R.id.weather_recyclerView)
        mainActivityViewModel.getLonLat().observeForever {
            weatherTast().execute()
            Log.d("MAIN", "mainActivityViewModel 7 ${mainActivityViewModel.getLonLat().value}")
        }
        return root
    }

    inner class weatherTast(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            Log.d("MAIN", "doInBackground 7")

            Log.d("MAIN", "https://api.openweathermap.org/data/2.5/onecall?lat=${mainActivityViewModel.getLonLat().value?.second}&lon=${mainActivityViewModel.getLonLat().value?.first}&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru")

            val response = try {
                URL("https://api.openweathermap.org/data/2.5/onecall?lat=${mainActivityViewModel.getLonLat().value?.second}&lon=${mainActivityViewModel.getLonLat().value?.first}&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru")
                    //"https://api.openweathermap.org/data/2.5/onecall?lat=55.7522&lon=37.6156&exclude=hourly,minutely&units=metric&appid=${API}&lang=ru")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception){
                null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let {
                try {
                    val root = binding.root
                    Log.d("MAIN", "OK 7")
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
                    recyclerView?.let {
                        it.layoutManager = LinearLayoutManager(context)
                        it.adapter = RecyclerAdapter(weatherList)
                    }

                } catch (e: Exception) {
                    Log.d("MAIN", e.message.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}