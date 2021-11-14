package com.example.myapplication.ui.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import kotlinx.android.synthetic.main.item_city.view.*

class CityRecyclerAdapter(private val items: List<String>, private val setSearchText: (String) -> Unit) : RecyclerView.Adapter<CityRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val cityName: TextView = itemView.city_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: CityRecyclerAdapter.ViewHolder, position: Int) {
        holder.cityName.text = items[position].uppercase()
        holder.cityName.setOnClickListener {
            Log.d("MAIN", items[position])
            setSearchText(items[position])
            Toast.makeText(holder.itemView.context, "Click search button to get city forecast", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}