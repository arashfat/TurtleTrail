package com.example.osm.utils

import android.location.Location
import com.example.osm.model.Step
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private var isCalculating = true

object RoutingHelper {

    fun findClosestStep(
        userLocation: Location,
        geometry: List<List<Double>>,
        steps: List<Step>,
        lastMatchedIndex: Int // Start from the last known location
    ): Pair<Step?, Int> {
        var closestStep: Step? = null
        var minDistance = Double.MAX_VALUE
        var newMatchedIndex = lastMatchedIndex

        for (step in steps) {
            val from = step.wayPoints.first()
            val to = step.wayPoints.last()

            // Skip if the whole step is behind current progress
            if (to < lastMatchedIndex) continue

            val startIndex = maxOf(from, lastMatchedIndex.toLong())
            for (i in startIndex..to) {
                if (i >= geometry.size) continue
                val routePoint = geometry[i.toInt()]
                val distance = haversine(
                    arrayListOf(userLocation.longitude, userLocation.latitude),
                    routePoint
                )

                if (distance < minDistance) {
                    minDistance = distance
                    closestStep = step
                    newMatchedIndex = i.toInt()
                }
            }
        }

        return Pair(closestStep, newMatchedIndex)
    }

    private fun haversine(coord1: List<Double>, coord2: List<Double>): Double {
        val R = 6371000.0 // Earth's radius in meters
        val dLat = Math.toRadians(coord2[1] - coord1[1])
        val dLon = Math.toRadians(coord2[0] - coord1[0])
        val lat1 = Math.toRadians(coord1[1])
        val lat2 = Math.toRadians(coord2[1])

        val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }
}