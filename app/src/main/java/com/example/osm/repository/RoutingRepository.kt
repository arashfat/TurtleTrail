package com.example.osm.repository

import com.example.osm.data.RoutingService
import com.example.osm.model.RoutingResultModel
import com.example.osm.network.BaseApiResponse
import com.example.osm.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RoutingRepository @Inject constructor(
    private val routingService: RoutingService
) : BaseApiResponse() {
    suspend fun getRoute(
        start: List<Double>,
        end: List<Double>
    ): Flow<NetworkResult<RoutingResultModel>> = flow {
        emit(safeApiCall {
            routingService.geoRouting(
                startPoint = start.joinToString(","),
                endPoint = end.joinToString(",")
            )
        })
    }
}