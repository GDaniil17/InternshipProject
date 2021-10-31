package com.example.myapplication.ui.today

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentTodayBinding
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {
    //TODO use only one call for getting today and week weather cast
    //TODO add search for specific city
    //TODO add loading cycle before showing weather
    //TODO extract weathers into viewModel

    val API = "afa2b1809a6d5d5aa9ea2f64420da228"
    val city = "Moscow"

    private lateinit var dashboardViewModel: TodayViewModel
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    var currentTemp: TextView? = null
    var minTemp: TextView? = null
    var maxTemp: TextView? = null
    var descriptionView: TextView? = null
    var day: TextView? = null

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
        weatherTast().execute()
        return root
    }

    inner class weatherTast(): AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            Log.d("MAIN", "doInBackground")
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${API}&lang=ru")
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
                    Log.d("MAIN", "OK")
                    val jsonObj = JSONObject(result)
                    val main = jsonObj.getJSONObject("main")
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                    val description = weather.getString("description")
                    val temp = "Now: "+main.getString("temp")+"°С"
                    val tempMin = "Min: "+main.getString("temp_min")+"°С"
                    val tempMax = "Max: "+main.getString("temp_max")+"°С"

                    descriptionView?.text = description
                    currentTemp?.text = temp
                    minTemp?.text = tempMin
                    maxTemp?.text = tempMax

                    val sdf = SimpleDateFormat("dd/MM/yyyy")
                    val netDate = Date(jsonObj.getString("dt").toLong() * 1000)
                    day?.text = sdf.format(netDate)
                } catch (e: Exception) {
                    Log.d("MAIN", e.message.toString())
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}