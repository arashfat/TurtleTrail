package com.example.osm.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationRepository @Inject constructor(@ApplicationContext context: Context) {
    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest =
        LocationRequest.create().setInterval(1000).setPriority(Priority.PRIORITY_HIGH_ACCURACY)

    @SuppressLint("MissingPermission")
    fun locationUpdate(): Flow<Location> = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(it).isSuccess
                }
            }
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        awaitClose {
            locationClient.removeLocationUpdates(locationCallback)
        }
    }
}