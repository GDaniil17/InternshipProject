package com.example.myapplication.ui.search

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.seven_days.SevenDaysViewModel
import com.example.myapplication.ui.seven_days.WeekWeatherData
import com.example.myapplication.ui.today.CurrentWeatherData
import com.example.myapplication.ui.today.TodayViewModel
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {

    val API = "7b7ebabc7f47bb63c0d5dc37e076bc8a"

    private lateinit var searchViewModel: SearchViewModel
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var searchBtn: ImageButton? = null
    private var textInputEditText: TextInputEditText? = null
    private val todayViewModel by activityViewModels<TodayViewModel>()
    private val sevenDaysViewModel by activityViewModels<SevenDaysViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private var city: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        searchBtn = root.findViewById(R.id.search_btn)
        textInputEditText = root.findViewById(R.id.textInputEditText)
        searchBtn?.setOnClickListener {
            city = textInputEditText?.text.toString()
            weatherTaskToday().execute()
        }
        return root
    }

    inner class weatherTaskToday(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${API}&lang=ru")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception){
                Log.d("MAIN", "doInBackground search today "+e.message.toString())
                null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            result?.let {
                try {
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
                    todayViewModel.setTodayWeather(
                        CurrentWeatherData(sdf.format(netDate),
                        temp, tempMin, tempMax, description, pictureLink, lon, lat, city)
                    )
                    mainActivityViewModel.setLonLat(lon.toDouble(), lat.toDouble())

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