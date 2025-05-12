package com.example.osm.repository

import com.example.osm.data.SearchService
import com.example.osm.model.SearchResultModel
import com.example.osm.network.BaseApiResponse
import com.example.osm.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchService: SearchService
) : BaseApiResponse() {
    fun getPlaces(query: String): Flow<NetworkResult<List<SearchResultModel>>> {
        return flow {
            emit(safeApiCall { searchService.searchPlaces(query) })
        }
    }
}