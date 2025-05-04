package com.example.osm.model

import com.google.gson.annotations.SerializedName

data class SearchResultModel (
    @SerializedName("place_id")
    val placeId: Long? = null,
    @SerializedName("licence")
    val licence: String? = null,
    @SerializedName("osm_type")
    val osmType: String? = null,
    @SerializedName("osm_id")
    val osmId: Long? = null,
    @SerializedName("lat")
    val latitude: Double? = null,
    @SerializedName("lon")
    val longitude: Double? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("addresstype")
    val addressType: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("display_name")
    val displayName: String? = null,
)