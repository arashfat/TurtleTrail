package com.example.osm.data

import com.example.osm.model.RoutingResultModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RoutingService {
    @GET("v2/directions/driving-car")
    suspend fun geoRouting(
        @Query("api_key") key: String = "5b3ce3597851110001cf6248f6f539c5ca3841428af0e4fa0ed1311d",
        @Query("start") startPoint: String,
        @Query("end") endPoint: String
    ): Response<RoutingResultModel>
}