package com.example.nearbymosque.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nearbymosque.Adapters.PlaceDataAdapter.vh
import com.example.nearbymosque.databinding.ItemRvBinding
import com.example.nearbymosque.databinding.ItemRvSearchBinding
import com.google.android.libraries.places.api.model.AutocompletePrediction

class Adapterim(
    private val context: Context,
    private val predictions: List<AutocompletePrediction>,
    private val rVAction: rvAction
) : RecyclerView.Adapter<Adapterim.vh>() {

    interface rvAction {
        fun onClick(position: Int, list: List<AutocompletePrediction>)
    }

    inner class vh(val itemRvBinding: ItemRvSearchBinding):RecyclerView.ViewHolder(itemRvBinding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): vh {
        return vh(ItemRvSearchBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: Adapterim.vh, position: Int) {
        val prediction = predictions[position]
        holder.itemRvBinding.imageMosque.setOnClickListener {
            rVAction.onClick(position,predictions)
        }
        holder.itemRvBinding.tvMosqueName.text = prediction.getPrimaryText(null).toString()
    }

    override fun getItemCount(): Int {
        return predictions.size
    }
}
