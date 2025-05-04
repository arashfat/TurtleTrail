package com.example.osm.data

import com.example.osm.model.SearchResultModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("search.php")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") f: String = "jsonv2",
        @Query("countrycodes") code: String = "ir",
        @Query("limit") limit: Int = 10
    ): Response<List<SearchResultModel>>
}