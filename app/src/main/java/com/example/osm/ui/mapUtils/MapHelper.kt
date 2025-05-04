package com.example.osm.ui.mapUtils

import org.osmdroid.util.BoundingBox

import org.osmdroid.util.GeoPoint

 fun getBoundingBox(start: GeoPoint, end: GeoPoint): BoundingBox? {
    val north: Double
    val south: Double
    val east: Double
    val west: Double
    if (start.latitude > end.latitude) {
        north = start.latitude
        south = end.latitude
    } else {
        north = end.latitude
        south = start.latitude
    }
    if (start.longitude > end.longitude) {
        east = start.longitude
        west = end.longitude
    } else {
        east = end.longitude
        west = start.longitude
    }
    return BoundingBox(north, east, south, west)
}

fun formatDistanceText(distance: Int): String {
    return if (distance < 1000) {
        "$distance m"
    } else {
        (String.format("%.1f", distance * 0.001)) + " km"
    }
}

fun convertSecondToText(second: Int): String {
    val hours = second / 3600
    val minutes = (second % 3600) / 60

    return "$hours hr $minutes min"
}