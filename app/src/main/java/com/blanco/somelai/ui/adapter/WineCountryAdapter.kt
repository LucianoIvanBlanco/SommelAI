package com.blanco.somelai.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.data.network.model.Country
import com.blanco.somelai.databinding.ItemWineCountryBinding
import com.bumptech.glide.Glide

class WineCountryAdapter (val action: (country: Country) -> Unit) :
    ListAdapter< Country, WineCountryAdapter.WineCountryViewHolder>(
        WineCountryItemCallBack
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineCountryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineCountryBinding.inflate(inflater, parent, false)
        return WineCountryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineCountryViewHolder, position: Int) {
        val country = getItem(position)
        holder.binding.root.setOnClickListener { action(country) }
        holder.binding.tvWineConuntryTitle.text = country.title

        Glide.with(holder.binding.root).load(country.image)
            .centerCrop()
            .into(holder.binding.ivCountryWine)


    }

    inner class WineCountryViewHolder(val binding: ItemWineCountryBinding) :
        RecyclerView.ViewHolder(binding.root)
}

object WineCountryItemCallBack: DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(oldItem: Country, newItem: Country): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Country, newItem: Country): Boolean {
        return oldItem == newItem
    }
}