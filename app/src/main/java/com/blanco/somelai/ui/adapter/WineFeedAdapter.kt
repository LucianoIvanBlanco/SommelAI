package com.blanco.somelai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.body.WineBody
import com.blanco.somelai.databinding.ItemWineListBinding
import com.bumptech.glide.Glide

class WineFeedAdapter(
    private val goToDetail: (wine: WineBody) -> Unit
) : ListAdapter<WineBody, WineFeedAdapter.WineFeedViewHolder>(WineFeedItemCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineFeedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineListBinding.inflate(inflater, parent, false)
        return WineFeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineFeedViewHolder, position: Int) {
        val wine = getItem(position)

        holder.binding.tvWineTitle.text = wine.wine + " " + wine.year
        holder.binding.tvWinery.text = wine.winery
        holder.binding.tvWineLocation.text = wine.country
        holder.binding.tvWineScoreItem.text = wine.rating

        holder.binding.root.setOnClickListener { goToDetail(wine) }

        Glide.with(holder.binding.root)
            .load(wine.image)
            .fitCenter()
            .error(R.drawable.ic_search)
            .into(holder.binding.ivWine)
    }

    inner class WineFeedViewHolder(val binding: ItemWineListBinding) :
        RecyclerView.ViewHolder(binding.root)
}

object WineFeedItemCallBack: DiffUtil.ItemCallback<WineBody>() {
    override fun areItemsTheSame(oldItem: WineBody, newItem: WineBody): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WineBody, newItem: WineBody): Boolean {
        return oldItem.wine == newItem.wine &&
                oldItem.year == newItem.year &&
                oldItem.winery == newItem.winery &&
                oldItem.country == newItem.country &&
                oldItem.rating == newItem.rating &&
                oldItem.pairing == newItem.pairing
    }
}