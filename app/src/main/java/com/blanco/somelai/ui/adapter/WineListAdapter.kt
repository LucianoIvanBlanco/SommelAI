package com.blanco.somelai.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.responses.Wine
import com.blanco.somelai.databinding.ItemWineListBinding
import com.bumptech.glide.Glide

class WineListAdapter(
    private val goToDetail: (wine: Wine) -> Unit
) : ListAdapter<Wine, WineListAdapter.WineViewHolder>(ItemCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWineListBinding.inflate(inflater, parent, false)
        return WineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        val wine = getItem(position)
        holder.binding.tvWineTitle.text = wine.winery // winery es el title
        holder.binding.tvWinery.text = wine.wine      // wine es la bodega
        holder.binding.tvWineLocation.text = wine.location
        holder.binding.tvWineScoreItem.text = wine.rating.average

        holder.binding.root.setOnClickListener { goToDetail(wine) }

        Glide.with(holder.binding.root)
            .load(wine.image)
            .fitCenter()
            .error(R.drawable.ic_search)
            .into(holder.binding.ivWine)
    }

    inner class WineViewHolder(val binding: ItemWineListBinding) :
        RecyclerView.ViewHolder(binding.root)

}

object ItemCallBack: DiffUtil.ItemCallback<Wine>() {
    override fun areItemsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Wine, newItem: Wine): Boolean {
        return oldItem.wine == newItem.wine &&
                oldItem.winery == newItem.winery &&
                oldItem.rating == newItem.rating &&
                oldItem.location == newItem.location

    }
}
