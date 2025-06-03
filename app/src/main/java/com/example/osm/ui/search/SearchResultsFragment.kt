package com.example.osm.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.osm.model.SearchResultModel
import com.example.osm.ui.main.MainViewModel

class SearchResultsFragment : Fragment() {
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MainContent()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observers()
        viewModel.searchPlaces(viewModel.searchQuery, viewModel.searchBox)
    }

    private fun observers() {

    }

    @Composable
    private fun MainContent() {
        val data = viewModel.places.observeAsState()

        data.value?.let {
            SearchResultsScreen(it)
        }
    }

    @Composable
    private fun SearchResultsScreen(items: List<SearchResultModel>) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.White, RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
                )
        ) {
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.size(24.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                items(items) {
                    SearchModel(it)
                }

            }
        }
    }

    @Composable
    private fun SearchModel(searchResult: SearchResultModel) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(8.dp)
        , verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = searchResult.name ?: "")
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = searchResult.displayName ?: "")
            }
        }
    }

    @Composable
    @Preview
    private fun Preview() {
        SearchModel(SearchResultModel(1111, "", displayName =  "test test", name = "Test"))
    }
}