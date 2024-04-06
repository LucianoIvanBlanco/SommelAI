package com.blanco.somelai.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.data.network.model.WineType
import com.blanco.somelai.databinding.ItemWineTypeBinding
import com.bumptech.glide.Glide

class WineTypeAdapter (val action: (wineType: WineType) -> Unit) :
    ListAdapter< WineType, WineTypeAdapter.WineTypeViewHolder>(
        WineTypeItemCallBack
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineTypeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineTypeBinding.inflate(inflater, parent, false)
        return WineTypeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineTypeViewHolder, position: Int) {
        val wineType = getItem(position)
        holder.binding.root.setOnClickListener { action(wineType) }
        holder.binding.tvWineType.text = wineType.title

        Glide.with(holder.binding.root).load(wineType.image)
            .centerCrop()
            .into(holder.binding.ivWineType)


    }

    inner class WineTypeViewHolder(val binding: ItemWineTypeBinding) :
        RecyclerView.ViewHolder(binding.root)
}

object WineTypeItemCallBack: DiffUtil.ItemCallback<WineType>() {
    override fun areItemsTheSame(oldItem: WineType, newItem: WineType): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WineType, newItem: WineType): Boolean {
        return oldItem == newItem
    }
}