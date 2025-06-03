package com.example.osm.utils

import android.location.Location
import com.example.osm.R
import com.example.osm.model.Point
import com.example.osm.model.Step
import com.example.osm.model.enum.ManeuverType.*
import org.osmdroid.views.MapView
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object RoutingHelper {

    fun findClosestStep(
        userLocation: Location,
        geometry: List<List<Double>>,
        steps: List<Step>,
        lastMatchedIndex: Int // Start from the last known location
    ): Pair<Int, Int> {
        var stepIndex = 0
        var minDistance = Double.MAX_VALUE
        var newMatchedIndex = lastMatchedIndex

        for ((index, step) in steps.withIndex()) {
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
                    stepIndex = index
                    newMatchedIndex = i.toInt()
                }
            }
        }

        return Pair(stepIndex, newMatchedIndex)
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

    fun distanceLeftInStep(
        geometry: List<List<Double>>,
        step: Step,
        currentIndex: Int
    ): Double {
        val from = maxOf(step.wayPoints.first(), currentIndex.toLong())
        val to = step.wayPoints.last()

        if (from >= to) return 0.0

        var total = 0.0
        for (i in from until to) {
            total += haversine(geometry[i.toInt()], geometry[i.toInt() + 1])
        }

        return total
    }

    fun getManeuverImage(type: Int): Int {
        return when(type) {
            TURN_LEFT.code, SHARP_LEFT.code -> R.drawable.ic_arrow_left_turn
            TURN_RIGHT.code, SHARP_RIGHT.code -> R.drawable.ic_arrow_right_turn
            SLIGHT_LEFT.code, KEEP_LEFT.code -> R.drawable.ic_arrow_slight_left
            SLIGHT_RIGHT.code, KEEP_RIGHT.code -> R.drawable.ic_arrow_slight_right
            U_TURN.code -> R.drawable.ic_arrow_u_turn_left
            ARRIVE.code -> R.drawable.ic_arrow_destination
            else -> R.drawable.ic_arrow_straight

        }
    }

    fun formatDistance(distanceMeters: Double): String {
        return when {
            distanceMeters >= 1000 -> {
                val km = distanceMeters / 1000.0
                String.format("%.1f km", km)
            }
            distanceMeters >= 100 -> {
                val rounded = (distanceMeters / 50).roundToInt() * 50
                "$rounded m"
            }
            else -> {
                val rounded = (distanceMeters / 20).roundToInt() * 20
                "$rounded m"
            }
        }
    }

    fun createBondingBox(map: MapView): List<Double> {
        val boundingBox = map.boundingBox

        val topLeft = Point(boundingBox.latNorth, boundingBox.lonWest)
        val bottomRight = Point(boundingBox.latSouth, boundingBox.lonEast)

        return arrayListOf(topLeft.lng, topLeft.lat, bottomRight.lng, bottomRight.lat)
    }
}