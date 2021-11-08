package com.example.myapplication.ui.search

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.myapplication.ui.today.API
import com.example.myapplication.ui.today.CurrentWeatherData
import com.example.myapplication.ui.today.TodayViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_search.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class SearchFragment : Fragment() {
    private lateinit var searchViewModel: SearchViewModel
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val todayViewModel by activityViewModels<TodayViewModel>()
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val searchFragmentViewModel by activityViewModels<SearchViewModel>()
    private var city: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        searchViewModel =
            ViewModelProvider(this).get(SearchViewModel::class.java)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root = binding.root
        if (searchFragmentViewModel.searchValue.isNotBlank()) {
            textInputEditText?.setText(searchFragmentViewModel.searchValue)
        }
        textInputEditText?.addTextChangedListener (object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                searchFragmentViewModel.searchValue = s.toString()
            }
        })


        search_btn?.setOnClickListener {
            city = textInputEditText?.text.toString()
            WeatherTaskToday().execute()
        }
        return root
    }

    @SuppressLint("StaticFieldLeak")
    inner class WeatherTaskToday(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${API}&lang=ru")
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