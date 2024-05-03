package com.blanco.somelai.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.responses.Rating
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.databinding.ItemWineBinding
import com.blanco.somelai.databinding.ItemWineTypeBinding
import com.bumptech.glide.Glide

class WineListAdapter : ListAdapter<Wine, WineListAdapter.WineViewHolder>(ItemCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineBinding.inflate(inflater, parent, false)
        return WineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        val wine = getItem(position)
        Log.d("WineListAdapter", "Wine title: ${wine.wine}")
        holder.binding.tvWineTitle.text = wine.wine
        holder.binding.tvWinery.text = wine.winery
        holder.binding.tvWineLocation.text = wine.location
        holder.binding.tvWineScoreItem.text = wine.rating.average

        Glide.with(holder.binding.root)
            .load(wine.image)
            .fitCenter()
            .error(R.drawable.ic_search) // Reemplaza con tu imagen de marcador de posición
            .into(holder.binding.ivWineImage)
    }

    inner class WineViewHolder(val binding: ItemWineBinding) :
        RecyclerView.ViewHolder(binding.root)

}

object ItemCallBack: DiffUtil.ItemCallback<Wine>() {
    override fun areItemsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem == newItem
    }
}
