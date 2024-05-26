package com.blanco.somelai.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blanco.somelai.R
import com.blanco.somelai.data.network.model.responses.Wine
import com.bumptech.glide.Glide

class WineSearchAdapter(
    private val goToDetail: (wine: Wine) -> Unit
) : RecyclerView.Adapter<WineSearchAdapter.WineViewHolder>() {

    private var wines: List<Wine> = emptyList()

    fun setWines(wines: List<Wine>) {
        this.wines = wines
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wine_list, parent, false)
        return WineViewHolder(view)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {
        holder.bind(wines[position], goToDetail)
    }

    override fun getItemCount(): Int = wines.size

    class WineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val wineNameTextView: TextView = itemView.findViewById(R.id.tv_wine_title)
        private val wineImage: ImageView = itemView.findViewById(R.id.iv_wine)
        private val wineWineryTextView: TextView = itemView.findViewById(R.id.tv_winery)
        private val wineLocationTextView: TextView = itemView.findViewById(R.id.tv_wine_location)
        private val wineScoreTextView: TextView = itemView.findViewById(R.id.tv_wine_score_item)

        fun bind(wine: Wine, goToDetail: (wine: Wine) -> Unit) {
            wineNameTextView.text = wine.winery
            wineWineryTextView.text = wine.wine
            wineLocationTextView.text = wine.location
            wineScoreTextView.text = wine.rating.average.toString() // Asegúrate de que sea una cadena

            Glide.with(itemView.context)
                .load(wine.image)
                .fitCenter()
                .error(R.drawable.ic_search)
                .into(wineImage)

            itemView.setOnClickListener {
                goToDetail(wine)
            }
        }
    }
}
