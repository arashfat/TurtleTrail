package com.example.osm.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.osm.databinding.FragmentSearchBinding
import com.example.osm.model.SearchResultModel
import com.example.osm.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment: Fragment() {
    private val searches = arrayListOf<SearchResultModel>()
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    var adapter: SearchAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setEditText()
        collectors()
    }

    private fun collectors() {
        viewModel.places.observe(viewLifecycleOwner) {
            searches.addAll(it)
            adapter?.update(searches)
        }
    }

    private fun setRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rcSearch.layoutManager = layoutManager
        adapter = SearchAdapter {
            viewModel.onSearchResultClick(it)
        }
        binding.rcSearch.adapter = adapter

    }

    private fun setEditText() {
        binding.edSearch.setOnEditorActionListener {_, id, _ ->
            if (id == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchPlaces(binding.edSearch.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }
    }

}