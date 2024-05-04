package com.example.nearbymosque.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.nearbymosque.R
import com.example.nearbymosque.databinding.ItemRvBinding
import com.karimov03.customsearchmap.Classs.PlaceData

class PlaceDataAdapter(private val context: Context, private val list: ArrayList<PlaceData>,val rvAction: RvAction) : RecyclerView.Adapter<PlaceDataAdapter.vh>() {
        inner class vh(val itemRvBinding: ItemRvBinding):RecyclerView.ViewHolder(itemRvBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(ItemRvBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int =list.size

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: vh, position: Int) {
        val mosque=list[position]
        holder.itemRvBinding.tvMosqueName.text=mosque.name
        holder.itemRvBinding.tvMosqueLocation.text= "Masofa: "+mosque.destination.toString()+" km"
        if (mosque.time=="true"){
            holder.itemRvBinding.tvBool.text="Hozir: Ochiq"
            holder.itemRvBinding.tvBool.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        else{
            holder.itemRvBinding.tvBool.text="Hozir: Noma'lum"
            holder.itemRvBinding.tvBool.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.orange))
        }

        holder.itemRvBinding.imageMosque.setOnClickListener {
            rvAction.onClick(list,position)
        }
    }
    interface RvAction{
        fun onClick(list: ArrayList<PlaceData>,position: Int)

    }

}
