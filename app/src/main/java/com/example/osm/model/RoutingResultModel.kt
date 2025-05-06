package com.example.osm.model

import com.google.gson.annotations.SerializedName

data class RoutingResultModel(
    val type: String,
    val metadata: Metadata,
    val bbox: List<Double>,
    val features: List<Feature>,
)
data class Metadata(
    val attribution: String,
    val service: String,
    val timestamp: Long,
    val query: Query,
    val engine: Engine,
)

data class Query(
    val coordinates: List<List<Double>>,
    val profile: String,
    val format: String,
)

data class Engine(
    val version: String,
    @SerializedName("build_date")
    val buildDate: String,
    @SerializedName("graph_date")
    val graphDate: String,
)

data class Feature(
    val bbox: List<Double>,
    val type: String,
    val properties: Properties,
    val geometry: Geometry,
)

data class Properties(
    val transfers: Long,
    val fare: Long,
    val segments: List<Segment>,
    @SerializedName("way_points")
    val wayPoints: List<Long>,
    val summary: Summary,
)

data class Segment(
    val distance: Double,
    val duration: Double,
    val steps: List<Step>,
)

data class Step(
    val distance: Double,
    val duration: Double,
    val type: Long,
    val instruction: String,
    val name: String,
    @SerializedName("way_points")
    val wayPoints: List<Long>,
    var isShown: Boolean = false
)

data class Summary(
    val distance: Double,
    val duration: Double,
)

data class Geometry(
    val coordinates: List<List<Double>>,
    val type: String,
)