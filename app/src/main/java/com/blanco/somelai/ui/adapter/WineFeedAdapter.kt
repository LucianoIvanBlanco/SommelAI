package com.blanco.somelai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.databinding.ItemWineFeedBinding
import com.bumptech.glide.Glide

class WineFeedAdapter (val deleteWine: (wine: Wine) -> Unit) :
    ListAdapter< Wine, WineFeedAdapter.WineFeedViewHolder>(
        WineFeedItemCallBack
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineFeedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineFeedBinding.inflate(inflater, parent, false)
        return WineFeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineFeedViewHolder, position: Int) {
        val wine = getItem(position)
        holder.binding.root.setOnClickListener { deleteWine(wine) }
        holder.binding.tvWineTitle.text = wine.wine
        holder.binding.tvWinery.text = wine.winery
        holder.binding.tvWineLocation.text = wine.location
        holder.binding.tvScoreItem.text = wine.rating.average

        holder.binding.btnDeleteFeed.setOnClickListener { deleteWine(wine)}


        Glide.with(holder.binding.root).load(wine.image)
            .centerCrop()
            .into(holder.binding.ivWineImage)


    }

    inner class WineFeedViewHolder(val binding: ItemWineFeedBinding) :
        RecyclerView.ViewHolder(binding.root)
}

object WineFeedItemCallBack: DiffUtil.ItemCallback<Wine>() {
    override fun areItemsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem == newItem
    }
}