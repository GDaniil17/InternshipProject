package com.example.myapplication.ui.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.ui.MainActivityViewModel
import com.example.myapplication.ui.today.API
import com.example.myapplication.ui.today.response.Response
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.URL
import java.util.*

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val job: Job = Job()
    private var mainHandler: Handler = Handler(Looper.getMainLooper())
    private val mainActivityViewModel by activityViewModels<MainActivityViewModel>()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val cities = listOf("abakan", "almetyevsk", "anadyr", "anapa", "arkhangelsk", "astrakhan", "barnaul", "belgorod", "beslan", "birobidzhan", "biysk", "blagoveshchensk", "bologoye", "bryansk", "chebarkul", "cheboksary", "chelyabinsk", "cherepovets", "cherkessk", "chistopol", "chita", "dmitrov", "dubna", "dzerzhinsk", "elista", "engels", "gatchina", "gdov", "gelendzhik", "gorno-altaysk", "grozny", "gudermes", "gus-khrustalny", "irkutsk", "ivanovo", "izhevsk", "kaliningrad", "kaluga", "kazan", "kemerovo", "khabarovsk", "khanty-mansiysk", "kislovodsk", "komsomolsk-on-amur", "kotlas", "krasnodar", "krasnoyarsk", "kurgan", "kursk", "kyzyl", "leninogorsk", "lensk", "lipetsk", "luga", "lyuban", "lyubertsy", "magadan", "makhachkala", "maykop", "miass", "mineralnye vody", "mirny", "moscow", "murmansk", "murom", "mytishchi", "naberezhnye chelny", "nadym", "nakhodka", "nalchik", "naryan-mar", "nazran", "nizhnekamsk", "nizhnevartovsk", "nizhny novgorod", "nizhny tagil", "norilsk", "novokuznetsk", "novosibirsk", "novy urengoy", "obninsk", "oktyabrsky", "omsk", "orekhovo-zuyevo", "orenburg", "oryol", "penza", "perm", "petropavlovsk-kamchatsky", "petrozavodsk", "podolsk", "pskov", "pyatigorsk", "rostov-on-don", "ryazan", "rybinsk", "saint petersburg", "salekhard", "samara", "saransk", "saratov", "severodvinsk", "shadrinsk", "shatura", "shuya", "smolensk", "sochi", "sol-iletsk", "stavropol", "surgut", "syktyvkar", "tambov", "tobolsk", "tolyatti", "tomsk", "tuapse", "tula", "tver", "tynda", "tyumen", "ufa", "ulan-ude", "ulyanovsk", "veliky novgorod", "veliky ustyug", "vladikavkaz", "vladimir", "vladivostok", "volgograd", "vologda", "vorkuta", "voronezh", "yakutsk", "yaroslavl", "yekaterinburg", "yelabuga", "yelets", "yessentuki", "yeysk", "yoshkar-ola", "yuzhno-sakhalinsk", "zlatoust")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val searchView = root.search_view
        val adapter = CityRecyclerAdapter(cities) { searchView.setText(it) }
        root.citiesList?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
        }
        Log.d("MAIN", searchView.toString())
        searchView?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(query: Editable?) {
                searchView.clearFocus()
                query?.let { query -> citiesList.adapter = CityRecyclerAdapter(cities.filter{ it.contains(query.toString().lowercase()) }, {searchView.setText(it)}) }
            }
        })
        root.search_btn.setOnClickListener {
            if (searchView.text?.isNotBlank() == true) {
                getResponse(searchView.text.toString())
            }
        }
        return binding.root
    }

    private fun doWorkAsync(city: String): Deferred<String?> = scope.async {
        val response = try {
            URL("https://api.openweathermap.org/data/2.5/weather?q=$city" +
                    "&units=metric&appid=$API&lang=ru")
                .readText(Charsets.UTF_8)
        } catch (e: Exception){
            showMsg("Please check the city name and Internet connection")
            Log.d("MAIN", "doInBackground "+e.message.toString())
            return@async null
        }
        showMsg("The request for changing city sent")
        return@async response
    }

    private fun getResponse(city: String) = scope.launch {
        try {
            val response = doWorkAsync(city).await()
            Log.d("MAIN", "!!! $response")
            response?.let {
                showMsg("$city was found")
                try {
                    val data = Gson().fromJson(response, Response::class.java)
                    val lon = data.coord.lon
                    val lat = data.coord.lat
                    mainActivityViewModel.setLonLat(lon, lat)
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