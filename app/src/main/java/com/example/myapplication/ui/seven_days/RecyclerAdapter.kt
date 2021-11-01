package com.example.myapplication.ui.seven_days

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import java.text.SimpleDateFormat
import java.util.*

class RecyclerAdapter(private val items: MutableList<WeekWeatherData>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val minTemp: TextView = itemView.findViewById(R.id.min_temp)
        val maxTemp: TextView = itemView.findViewById(R.id.max_temp)
        val morningTemp: TextView = itemView.findViewById(R.id.morning_temp)
        val dayTemp: TextView = itemView.findViewById(R.id.day_temp)
        val eveningTemp: TextView = itemView.findViewById(R.id.eve_temp)
        val nightTemp: TextView = itemView.findViewById(R.id.night_temp)
        val description: TextView = itemView.findViewById(R.id.weather_description)
        val day: TextView = itemView.findViewById(R.id.day)
        val weatherImg: ImageView = itemView.findViewById(R.id.weather_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].let {
            holder.minTemp.text = "Min: ${it.minTemp.toString()}°С"
            holder.maxTemp.text = "Max: ${it.maxTemp.toString()}°С"
            holder.morningTemp.text = "Morning: ${it.morningTemp.toString()}°С"
            holder.dayTemp.text = "Day: ${it.dayTemp.toString()}°С"
            holder.eveningTemp.text = "Evening: ${it.eveningTemp.toString()}°С"
            holder.nightTemp.text = "Night: ${it.nightTemp.toString()}°С"
            holder.description.text = it.description
            Glide.with(holder.itemView.context).load(it.img).into(holder.weatherImg)
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(it.day * 1000)
            holder.day.text = sdf.format(netDate)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}