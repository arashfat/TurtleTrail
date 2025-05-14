package com.example.osm.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.osm.R
import com.example.osm.databinding.ItemSearchPlaceBinding

class SearchPlaceAdapter(private val onClick:(String) -> Unit) : RecyclerView.Adapter<SearchPlaceAdapter.SearchPlaceViewHolder>() {
    private val items = arrayListOf<SearchPlaceModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceViewHolder {
        return SearchPlaceViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_place, parent, false), onClick
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchPlaceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateAdapter(newItems: List<SearchPlaceModel>) {
        items.clear()
        items.addAll(newItems)
        notifyItemRangeChanged(0, items.size)
    }

    class SearchPlaceViewHolder(parent: View, private val onClick:(String) -> Unit) : RecyclerView.ViewHolder(parent) {
        private val binding = ItemSearchPlaceBinding.bind(parent)
        fun bind(place: SearchPlaceModel) {
            binding.ivSearch.setImageResource(place.icon)
            binding.tvTitleSearch.text = place.name

            binding.root.setOnClickListener {
                onClick.invoke(place.searchKey)
            }
        }
    }
}

