package com.example.osm.repository

import com.example.osm.data.SearchService
import com.example.osm.model.Point
import com.example.osm.model.ReverseGeoResponse
import com.example.osm.model.SearchResultModel
import com.example.osm.network.BaseApiResponse
import com.example.osm.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val searchService: SearchService
) : BaseApiResponse() {
    fun getPlaces(query: String, boundingBox: List<Double>? = null): Flow<NetworkResult<List<SearchResultModel>>> {
        return flow {
            val bbox = boundingBox?.let {
                it.joinToString(",")
            }
            emit(safeApiCall { searchService.searchPlaces(query, boundingBox = bbox) })
        }
    }

    fun reverseGeoCoding(point: Point): Flow<NetworkResult<ReverseGeoResponse>> {
        return flow {
            emit(safeApiCall { searchService.reverseGeoCoding(
                point.lat, point.lng
            ) })
        }
    }
}