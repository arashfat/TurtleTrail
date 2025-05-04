package com.example.osm.ui.searcResult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.osm.R
import com.example.osm.databinding.ItemSearchBinding
import com.example.osm.model.SearchResultModel

class SearchAdapter(private val onclick: ((SearchResultModel) -> Unit)? = null): RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var items = arrayListOf<SearchResultModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false), onclick
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun update(searches: List<SearchResultModel>) {
        items.clear()
        items.addAll(searches)
        notifyDataSetChanged()
    }

    class SearchViewHolder(parent: View, private val onclick: ((SearchResultModel) -> Unit)? = null): RecyclerView.ViewHolder(parent) {
        private val binding = ItemSearchBinding.bind(parent)
        fun bind(search: SearchResultModel) {
            binding.tvSearch.text = search.displayName

            binding.root.setOnClickListener {
                onclick?.invoke(search)
            }
        }
    }
}