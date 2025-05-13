package com.example.osm.model

import com.google.gson.annotations.SerializedName

data class ReverseGeoResponse(
    val name: String? = null,
    @SerializedName("display_name")
    val fullDisplayName: String? = null,
    val lat: Double,
    val lon: Double
)